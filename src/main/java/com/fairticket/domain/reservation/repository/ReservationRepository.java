package com.fairticket.domain.reservation.repository;

import com.fairticket.domain.reservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 나중에 '내 예약 조회' 등을 위해 필요하면 메소드 추가
}