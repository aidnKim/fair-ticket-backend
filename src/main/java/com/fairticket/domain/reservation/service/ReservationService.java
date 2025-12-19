package com.fairticket.domain.reservation.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fairticket.domain.concert.model.ConcertSchedule;
import com.fairticket.domain.concert.model.Seat;
import com.fairticket.domain.concert.model.SeatStatus;
import com.fairticket.domain.concert.repository.ConcertScheduleRepository;
import com.fairticket.domain.concert.repository.SeatRepository;
import com.fairticket.domain.reservation.dto.ReservationCreateRequestDto;
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

    @Transactional
    public Long createReservation(String email, ReservationCreateRequestDto requestDto) {
        // 1. 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 2. 스케줄 조회
        ConcertSchedule schedule = scheduleRepository.findById(requestDto.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다."));

        // 3. 좌석 조회 (비관적 락 적용)
        // 이 줄이 실행되는 순간, 트랜잭션이 끝날 때까지 다른 사람은 이 seatId를 건들지 못함.
        Seat seat = seatRepository.findByIdWithLock(requestDto.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("좌석을 찾을 수 없습니다."));

        // 4. 이미 예약된 좌석인지 확인
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new IllegalArgumentException("이미 선택된 좌석입니다.");
        }

        // 5. 좌석 상태 변경 (AVAILABLE -> TEMPORARY_RESERVED)
        // Seat 엔티티 안에 만들어둔 reserve() 메소드 사용
        seat.reserve();
        
        // (수정) 변경 사항을 즉시 DB에 반영 (플러시)
        // Dirty Checking을 기다리지 않고 강제로 Update 쿼리를 날림
        seatRepository.saveAndFlush(seat);

        // 6. 예약 생성 및 저장
        Reservation reservation = Reservation.builder()
                .user(user)
                .seat(seat)
                .schedule(schedule)
                .reservationTime(LocalDateTime.now())
                .build();

        return reservationRepository.save(reservation).getId();
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
        }
        
        if (!expiredReservations.isEmpty()) {
            log.info("[Scheduler] 만료된 예약 {}건을 취소했습니다.", expiredReservations.size());
        }
    }
}