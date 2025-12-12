package com.fairticket.domain.payment.service;

import com.fairticket.domain.concert.model.Seat;
import com.fairticket.domain.payment.dto.PaymentCreateRequestDto;
import com.fairticket.domain.payment.model.Payment;
import com.fairticket.domain.payment.model.PaymentStatus;
import com.fairticket.domain.payment.repository.PaymentRepository;
import com.fairticket.domain.reservation.model.Reservation;
import com.fairticket.domain.reservation.repository.ReservationRepository;
import com.fairticket.domain.user.model.User;
import com.fairticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long processPayment(String email, PaymentCreateRequestDto requestDto) {
        // 1. 유저 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 2. 예약 정보 확인
        Reservation reservation = reservationRepository.findById(requestDto.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 3. 본인 예약인지 검증
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 예약만 결제할 수 있습니다.");
        }

        // 4. 결제 금액 검증 (예: 좌석 가격과 요청 금액이 같은지)
        // 실제로는 PG사 검증 로직이 들어가야 함
        if (reservation.getSeat().getPrice().compareTo(requestDto.getAmount()) != 0) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
        }

        // 5. 결제 진행 (PG사 연동 생략 -> 성공 가정)
        // ----------------------------------------------------
        // [핵심 로직] 상태 변경
        // 1) 예약 상태: PENDING -> PAID
        reservation.completePayment();
        
        // 2) 좌석 상태: TEMPORARY_RESERVED -> SOLD
        reservation.getSeat().confirmSold();
        // ----------------------------------------------------

        // 6. 결제 이력 저장
        Payment payment = Payment.builder()
                .user(user)
                .reservation(reservation)
                .amount(requestDto.getAmount())
                .status(PaymentStatus.COMPLETED)
                .build();

        return paymentRepository.save(payment).getId();
    }
}