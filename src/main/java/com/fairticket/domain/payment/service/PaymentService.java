package com.fairticket.domain.payment.service;

import com.fairticket.domain.concert.model.Seat;
import com.fairticket.domain.concert.service.SeatAvailabilityService;
import com.fairticket.domain.payment.dto.PaymentCreateRequestDto;
import com.fairticket.domain.payment.dto.PortOnePaymentResponseDto;
import com.fairticket.domain.payment.model.Payment;
import com.fairticket.domain.payment.model.PaymentStatus;
import com.fairticket.domain.payment.repository.PaymentRepository;
import com.fairticket.domain.reservation.model.Reservation;
import com.fairticket.domain.reservation.model.ReservationStatus;
import com.fairticket.domain.reservation.repository.ReservationRepository;
import com.fairticket.domain.user.model.User;
import com.fairticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final PortOneService portOneService;
    private final SeatAvailabilityService seatAvailabilityService;

    //결제 처리
    @Transactional
    public Long processPayment(String email, PaymentCreateRequestDto requestDto) {
        try {
            // 1. 유저 확인
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            // 2. 예약 정보 확인
            Reservation reservation = reservationRepository.findById(requestDto.getReservationId())
                    .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
            // 예약 상태 검증
            if (reservation.getStatus() != ReservationStatus.PENDING) {
                throw new IllegalArgumentException("이미 처리되었거나 만료된 예약입니다.");
            }

            // 3. 본인 예약인지 검증
            if (!reservation.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("본인의 예약만 결제할 수 있습니다.");
            }

            // 4. 포트원 결제 검증
            PortOnePaymentResponseDto portOneData = portOneService.getPaymentInfo(requestDto.getImpUid());
            
            // 검증 1: 결제 상태
            if (!"paid".equals(portOneData.getStatus())) {
                throw new IllegalArgumentException("결제가 완료되지 않았습니다.");
            }
            // 검증 2: 결제 금액
            if (portOneData.getAmount().compareTo(reservation.getSeat().getPrice()) != 0) {
                throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
            }
            // 검증 3: 주문번호
            if (!requestDto.getMerchantUid().equals(portOneData.getMerchantUid())) {
                throw new IllegalArgumentException("주문번호가 일치하지 않습니다.");
            }

            // 5. [핵심 로직] 상태 변경
            // 1) 예약 상태: PENDING -> PAID
            reservation.completePayment();
            
            // 2) 좌석 상태: TEMPORARY_RESERVED -> SOLD
            reservation.getSeat().confirmSold();

            // 6. 결제 이력 저장
            Payment payment = Payment.builder()
                    .user(user)
                    .reservation(reservation)
                    .amount(portOneData.getAmount())
                    .status(PaymentStatus.COMPLETED)
                    .impUid(requestDto.getImpUid())
                    .merchantUid(requestDto.getMerchantUid())
                    .build();

            return paymentRepository.save(payment).getId();
            
        } catch (Exception e) {
            // 어떤 예외든 발생하면 포트원 결제 취소 시도
            try {
                if (requestDto.getImpUid() != null) {
                    portOneService.cancelPayment(requestDto.getImpUid(), "서버 오류: " + e.getMessage());
                }
            } catch (Exception cancelEx) {
                // 취소도 실패하면 로그 기록 (수동 처리 필요)
                log.error("❌ 결제 자동 취소 실패! impUid: {}, 원인: {}", 
                          requestDto.getImpUid(), cancelEx.getMessage());
            }
            // 원래 예외 다시 던지기
            throw e;
        }
    }
    
    // 테스트 결제 (포트원 없이)
    @Transactional
    public Long processTestPayment(String email, Long reservationId) {
        // 1. 유저 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 2. 예약 정보 확인
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        // 예약 상태 검증
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리되었거나 만료된 예약입니다.");
        }

        // 3. 본인 예약인지 검증
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 예약만 결제할 수 있습니다.");
        }

        // 4. 포트원 검증 스킵! 바로 상태 변경
        // 예약 상태: PENDING -> PAID
        reservation.completePayment();
        
        // 좌석 상태: TEMPORARY_RESERVED -> SOLD
        reservation.getSeat().confirmSold();

        // 5. 테스트 결제 기록 생성
        Payment payment = Payment.builder()
                .user(user)
                .reservation(reservation)
                .amount(reservation.getSeat().getPrice())
                .status(PaymentStatus.COMPLETED)
                .impUid("TEST_" + System.currentTimeMillis())
                .merchantUid("ORD-TEST-" + System.currentTimeMillis())
                .build();

        return paymentRepository.save(payment).getId();
    }
    
    //주문번호 생성
    public String createOrderNum() {
        return "ORD-" + UUID.randomUUID().toString();
    }
    
    //결제 취소
    @Transactional
    public void cancelPayment(String email, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 본인 확인
        if (!payment.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("본인의 결제만 취소할 수 있습니다.");
        }

        // 1. 포트원 결제 취소 API 호출 (테스트 결제면 스킵)
        if (!payment.getImpUid().startsWith("TEST_")) {
            portOneService.cancelPayment(payment.getImpUid(), "사용자 요청에 의한 취소");
        }

        // 2. 결제 상태 변경 (COMPLETED -> REFUNDED)
        payment.cancel();

        // 3. 예약 상태 변경 (PAID -> CANCELLED)
        payment.getReservation().cancel();

        // 4. 좌석 상태 변경 (SOLD -> AVAILABLE)
        payment.getReservation().getSeat().cancel();
        
        // 5. 잔여 좌석 증가
        payment.getReservation().getSchedule().increaseAvailableSeats();
        seatAvailabilityService.increaseSeats(payment.getReservation().getSchedule().getId());
    }
}