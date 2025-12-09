package com.fairticket.domain.concert.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fairticket.domain.concert.dto.ConcertCreateRequestDto;
import com.fairticket.domain.concert.dto.ConcertResponseDto;
import com.fairticket.domain.concert.dto.ScheduleCreateRequestDto;
import com.fairticket.domain.concert.dto.SeatResponseDto;
import com.fairticket.domain.concert.service.ConcertService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    // [관리자] 공연 생성
    @PostMapping
    public ResponseEntity<String> createConcert(@RequestBody ConcertCreateRequestDto requestDto) {
        Long concertId = concertService.createConcert(requestDto);
        return ResponseEntity.ok("공연 생성 완료! ID: " + concertId);
    }

    // [관리자] 공연에 스케줄 추가 (좌석 자동 생성)
    @PostMapping("/{concertId}/schedules")
    public ResponseEntity<String> addSchedule(@PathVariable Long concertId,
                                              @RequestBody ScheduleCreateRequestDto requestDto) {
        Long scheduleId = concertService.addSchedule(concertId, requestDto);
        return ResponseEntity.ok("스케줄 및 좌석 생성 완료! ID: " + scheduleId);
    }
    
    // [사용자] 공연 목록 조회
    @GetMapping
    public ResponseEntity<List<ConcertResponseDto>> getConcerts() {
        return ResponseEntity.ok(concertService.getConcerts());
    }

    // [사용자] 특정 스케줄의 좌석 조회 (예약할 때 필수!)
    @GetMapping("/{scheduleId}/seats")
    public ResponseEntity<List<SeatResponseDto>> getSeats(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(concertService.getSeats(scheduleId));
    }
}