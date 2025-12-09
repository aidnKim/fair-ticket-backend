package com.fairticket.domain.concert.repository;
import com.fairticket.domain.concert.model.ConcertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {
}