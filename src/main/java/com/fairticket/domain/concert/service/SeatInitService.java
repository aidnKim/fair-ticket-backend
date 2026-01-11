package com.fairticket.domain.concert.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fairticket.domain.concert.model.ConcertSchedule;
import com.fairticket.domain.concert.model.Seat;
import com.fairticket.domain.concert.model.SeatGrade;
import com.fairticket.domain.concert.model.SeatStatus;
import com.fairticket.domain.concert.model.Venue;
import com.fairticket.domain.concert.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatInitService {

    private final SeatRepository seatRepository;

    @Transactional
    public void createSeatsForSchedule(ConcertSchedule schedule, Venue venue) {
        List<Seat> seats = new ArrayList<>();

        for (int rowIdx = 0; rowIdx < venue.getTotalRows(); rowIdx++) {
            String row = String.valueOf((char) ('A' + rowIdx));

            for (int col = 1; col <= venue.getSeatsPerRow(); col++) {
                SeatGrade grade = venue.isVipRow(row) ? SeatGrade.VIP : SeatGrade.R;
                BigDecimal price = (grade == SeatGrade.VIP) 
                    ? new BigDecimal("100") 
                    : new BigDecimal("100");

                Seat seat = Seat.builder()
                        .schedule(schedule)
                        .seatRow(row)
                        .seatCol(col)
                        .grade(grade)
                        .price(price)
                        .status(SeatStatus.AVAILABLE)
                        .build();

                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
    }
}
