package com.fairticket.domain.concert.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fairticket.domain.concert.model.Seat;

import jakarta.persistence.LockModeType;

public interface SeatRepository extends JpaRepository<Seat, Long> {

	// 1. 단순 조회용 (락 없음 - 여러 명이 동시에 봐도 됨)
    List<Seat> findByScheduleIdOrderBySeatNoAsc(Long scheduleId);
    
    // 예약 선점용 (비관적 락 적용)
    // PESSIMISTIC_WRITE: 다른 트랜잭션이 읽지도, 쓰지도 못하게 막음 (가장 강력한 락)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :seatId")
    Optional<Seat> findByIdWithLock(@Param("seatId") Long seatId);
}