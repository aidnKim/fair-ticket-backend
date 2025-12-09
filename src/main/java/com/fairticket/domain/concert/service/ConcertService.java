package com.fairticket.domain.concert.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fairticket.domain.concert.dto.ConcertCreateRequestDto;
import com.fairticket.domain.concert.dto.ConcertResponseDto;
import com.fairticket.domain.concert.dto.ScheduleCreateRequestDto;
import com.fairticket.domain.concert.dto.SeatResponseDto;
import com.fairticket.domain.concert.model.Concert;
import com.fairticket.domain.concert.model.ConcertSchedule;
import com.fairticket.domain.concert.model.Seat;
import com.fairticket.domain.concert.model.SeatGrade;
import com.fairticket.domain.concert.model.SeatStatus;
import com.fairticket.domain.concert.repository.ConcertRepository;
import com.fairticket.domain.concert.repository.ConcertScheduleRepository;
import com.fairticket.domain.concert.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;

    // 1. 공연 생성
    @Transactional
    public Long createConcert(ConcertCreateRequestDto requestDto) {
        Concert concert = Concert.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .build();

        return concertRepository.save(concert).getId();
    }

    // 2. 스케줄 추가 및 좌석 자동 생성
    @Transactional
    public Long addSchedule(Long concertId, ScheduleCreateRequestDto requestDto) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("공연을 찾을 수 없습니다."));

        // 스케줄 저장
        ConcertSchedule schedule = ConcertSchedule.builder()
                .concert(concert)
                .concertDate(requestDto.getConcertDate())
                .totalSeats(requestDto.getTotalSeats())
                .build();
        
        scheduleRepository.save(schedule);

        // 좌석 자동 생성 로직 (MVP용 단순화)
        // 1~10번: VIP, 11~20번: R, 나머지: S 등급으로 자동 배정
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= requestDto.getTotalSeats(); i++) {
            SeatGrade grade = SeatGrade.S; // 기본 S
            BigDecimal price = new BigDecimal(50000); // 기본 5만원

            if (i <= 10) {
                grade = SeatGrade.VIP;
                price = new BigDecimal(150000);
            } else if (i <= 20) {
                grade = SeatGrade.R;
                price = new BigDecimal(100000);
            }

            Seat seat = Seat.builder()
                    .schedule(schedule)
                    .seatNo(i)
                    .grade(grade)
                    .price(price)
                    .status(SeatStatus.AVAILABLE) // 초기 상태는 '구매 가능'
                    .build();
            seats.add(seat);
        }

        seatRepository.saveAll(seats); // 한 번에 저장 (Bulk Insert)

        return schedule.getId();
    }
    
    // 3. 공연 목록 조회
    @Transactional(readOnly = true)
    public List<ConcertResponseDto> getConcerts() {
        return concertRepository.findAll().stream()
                .map(ConcertResponseDto::from)
                .collect(Collectors.toList());
    }

    // 4. 특정 스케줄의 좌석 조회
    @Transactional(readOnly = true)
    public List<SeatResponseDto> getSeats(Long scheduleId) {
        return seatRepository.findByScheduleIdOrderBySeatNoAsc(scheduleId).stream()
                .map(SeatResponseDto::from)
                .collect(Collectors.toList());
    }
    
    
}