package com.fairticket.domain.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fairticket.domain.concert.model.ConcertSchedule;
import com.fairticket.domain.concert.model.Seat;
import com.fairticket.domain.concert.model.SeatStatus;
import com.fairticket.domain.concert.repository.ConcertScheduleRepository;
import com.fairticket.domain.concert.repository.SeatRepository;
import com.fairticket.domain.concert.service.SeatAvailabilityService;
import com.fairticket.domain.payment.model.Payment;
import com.fairticket.domain.payment.repository.PaymentRepository;
import com.fairticket.domain.payment.service.PaymentService;
import com.fairticket.domain.reservation.dto.ReservationCreateRequestDto;
import com.fairticket.domain.reservation.dto.ReservationCreateResponseDto;
import com.fairticket.domain.reservation.dto.ReservationResponseDto;
import com.fairticket.domain.reservation.model.Reservation;
import com.fairticket.domain.reservation.model.ReservationStatus;
import com.fairticket.domain.reservation.repository.ReservationRepository;
import com.fairticket.domain.user.model.User;
import com.fairticket.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ConcertScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final RedissonClient redissonClient;
    private final SeatAvailabilityService seatAvailabilityService;

    @CacheEvict(value = "seats", key = "#requestDto.scheduleId")
    @Transactional
    public ReservationCreateResponseDto createReservation(String email, ReservationCreateRequestDto requestDto) {
        // 분산락 키 생성
        String lockKey = "lock:seat:" + requestDto.getSeatId();
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 락 획득 시도 (최대 3초 대기, 획득 후 5초 유지)
            boolean isLocked = lock.tryLock(3, 5, TimeUnit.SECONDS);
            
            if (!isLocked) {
                throw new IllegalStateException("다른 사용자가 해당 좌석을 예약 중입니다. 잠시 후 다시 시도해주세요.");
            }
            
            // 1. 유저 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
            // 2. 스케줄 조회
            ConcertSchedule schedule = scheduleRepository.findById(requestDto.getScheduleId())
                    .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다."));
            // 3. 좌석 조회 (비관적 락 제거, 일반 조회로 변경)
            Seat seat = seatRepository.findById(requestDto.getSeatId())
                    .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));
            // 4. 이미 예약된 좌석인지 확인
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalArgumentException("이미 선택된 좌석입니다.");
            }
            // 5. 좌석 상태 변경
            seat.reserve();
            seatRepository.saveAndFlush(seat);
            
            // 6. 잔여 좌석 감소
            schedule.decreaseAvailableSeats();
            seatAvailabilityService.decreaseSeats(requestDto.getScheduleId());
            // 7. 예약 생성 및 저장
            Reservation reservation = Reservation.builder()
                    .user(user)
                    .seat(seat)
                    .schedule(schedule)
                    .reservationTime(LocalDateTime.now())
                    .build();
            Reservation saved = reservationRepository.save(reservation);
            return ReservationCreateResponseDto.builder()
                    .reservationId(saved.getId())
                    .expireTime(saved.getExpireTime())
                    .build();
                    
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 오류가 발생했습니다.");
        } finally {
            // 락 해제 (본인이 획득한 락만 해제)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    // 만료된 예약 일괄 취소 (스케줄러가 호출)
    @Transactional
    public void cancelExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 만료된 예약들 찾기 (PENDING 상태이고, 만료시간이 지난 것)
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpireTimeBefore(ReservationStatus.PENDING, now);

        for (Reservation reservation : expiredReservations) {
            // 2. 예약 상태 변경 (PENDING -> CANCELLED)
            reservation.cancel();
            
            // 3. 좌석 상태 변경 (TEMPORARY_RESERVED -> AVAILABLE)
            // 다시 남들이 살 수 있게 풀어줌
            reservation.getSeat().cancel();
            
            // 잔여 좌석 증가
            reservation.getSchedule().increaseAvailableSeats();
            seatAvailabilityService.increaseSeats(reservation.getSchedule().getId());
        }
        
        if (!expiredReservations.isEmpty()) {
            log.info("[Scheduler] 만료된 예약 {}건을 취소했습니다.", expiredReservations.size());
        }
    }
    
    // 회원 별 예약 내역 조회
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getMyReservations(String email) {
        List<Reservation> reservations = reservationRepository.findByUserEmailOrderByReservationTimeDesc(email);
        
        return reservations.stream()
            .map(ReservationResponseDto::from)
            .toList();
    }
    
    // 예매 취소 (reservationId로)
    @Transactional
    public void cancelReservation(String email, Long reservationId) {
        // reservationId로 payment 찾기
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        paymentService.cancelPayment(email, payment.getId());
    }
}