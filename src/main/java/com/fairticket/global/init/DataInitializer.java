package com.fairticket.global.init;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fairticket.domain.concert.model.ConcertSchedule;
import com.fairticket.domain.concert.model.Venue;
import com.fairticket.domain.concert.repository.ConcertScheduleRepository;
import com.fairticket.domain.concert.repository.SeatRepository;
import com.fairticket.domain.concert.service.SeatInitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ConcertScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final SeatInitService seatInitService;

    @Override
    @Transactional
    public void run(String... args) {
        List<ConcertSchedule> schedules = scheduleRepository.findAll();

        for (ConcertSchedule schedule : schedules) {
            // 이미 좌석이 있으면 스킵
            if (seatRepository.existsBySchedule(schedule)) {
                continue;
            }

            Venue venue = schedule.getConcert().getVenue();
            if (venue != null) {
                seatInitService.createSeatsForSchedule(schedule, venue);
                log.info("스케줄 {} 좌석 생성 완료", schedule.getId());
            }
        }
    }
}