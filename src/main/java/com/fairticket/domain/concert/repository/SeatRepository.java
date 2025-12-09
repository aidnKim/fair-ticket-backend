package com.fairticket.domain.concert.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fairticket.domain.concert.model.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    // 나중에 비관적 락 적용할 곳 (지금은 비워둠)
	
	// 특정 스케줄(scheduleId)에 해당하는 좌석 리스트 조회
    // 좌석 번호 순서대로 정렬해서 가져오기
    List<Seat> findByScheduleIdOrderBySeatNoAsc(Long scheduleId);
}