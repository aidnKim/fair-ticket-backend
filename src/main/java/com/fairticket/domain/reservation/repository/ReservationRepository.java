package com.fairticket.domain.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fairticket.domain.reservation.model.Reservation;
import com.fairticket.domain.reservation.model.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	// 만료된 예약 조회 쿼리 메소드
    // SELECT * FROM reservation WHERE status = 'PENDING' AND expire_time < :now
    List<Reservation> findByStatusAndExpireTimeBefore(ReservationStatus status, LocalDateTime now);
    
    // 회원 별 예약 내역 조회
    List<Reservation> findByUserEmailOrderByReservationTimeDesc(String email);
}