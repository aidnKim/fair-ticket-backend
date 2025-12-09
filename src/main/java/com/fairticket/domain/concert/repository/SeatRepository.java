package com.fairticket.domain.concert.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fairticket.domain.concert.model.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    // 나중에 비관적 락 적용할 곳 (지금은 비워둠)
	
	// 1. 단순 조회용 (락 없음 - 여러 명이 동시에 봐도 됨)
    List<Seat> findByScheduleIdOrderBySeatNoAsc(Long scheduleId);
}