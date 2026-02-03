package com.fairticket.domain.concert.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fairticket.domain.concert.dto.ConcertCreateRequestDto;
import com.fairticket.domain.concert.dto.ConcertDetailResponseDto;
import com.fairticket.domain.concert.dto.ConcertResponseDto;
import com.fairticket.domain.concert.dto.ScheduleCreateRequestDto;
import com.fairticket.domain.concert.dto.SeatResponseDto;
import com.fairticket.domain.concert.model.Concert;
import com.fairticket.domain.concert.model.ConcertSchedule;
import com.fairticket.domain.concert.model.Seat;
import com.fairticket.domain.concert.model.SeatGrade;
import com.fairticket.domain.concert.model.SeatStatus;
import com.fairticket.domain.concert.model.Venue;
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
    private final SeatAvailabilityService seatAvailabilityService;

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
        
        // 스케줄 저장 후 Redis 초기화
        scheduleRepository.save(schedule);
        seatAvailabilityService.initializeSeats(schedule.getId(), requestDto.getTotalSeats());

        // 좌석 자동 생성 로직
        Venue venue = concert.getVenue();
        if (venue != null) {
            List<Seat> seats = new ArrayList<>();
            
            for (int rowIdx = 0; rowIdx < venue.getTotalRows(); rowIdx++) {
                String row = String.valueOf((char) ('A' + rowIdx));
                
                for (int col = 1; col <= venue.getSeatsPerRow(); col++) {
                    SeatGrade grade = venue.isVipRow(row) ? SeatGrade.VIP : SeatGrade.R;
                    BigDecimal price = (grade == SeatGrade.VIP) 
                        ? new BigDecimal("150000") 
                        : new BigDecimal("120000");
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
        return schedule.getId();
    }
    
    // 3. 공연 목록 조회
    @Cacheable(value = "concerts")
    @Transactional(readOnly = true)
    public List<ConcertResponseDto> getConcerts() {
        return concertRepository.findAll().stream()
                .map(ConcertResponseDto::from)
                .collect(Collectors.toList());
    }

    // 4. 특정 스케줄의 좌석 조회
    @Cacheable(value = "seats", key = "#scheduleId")
    @Transactional(readOnly = true)
    public List<SeatResponseDto> getSeats(Long scheduleId) {
        return seatRepository.findByScheduleIdOrderBySeatRowAscSeatColAsc(scheduleId).stream()
                .map(SeatResponseDto::from)
                .collect(Collectors.toList());
    }
    
    // 5. 특정 콘서트 세부 내용 조회
    // 기본 정보만 캐시 (엔티티)
    @Cacheable(value = "concertBasic", key = "#concertId")
    @Transactional(readOnly = true)
    public Concert getConcertBasic(Long concertId) {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공연입니다."));
    }

    // 상세 조회 (캐시 X) - 캐시된 기본 정보 + 실시간 잔여석
    @Transactional(readOnly = true)
    public ConcertDetailResponseDto getConcertDetail(Long concertId) {
        Concert concert = getConcertBasic(concertId);  // 캐시에서
        return new ConcertDetailResponseDto(concert, seatAvailabilityService);
    }
    
}