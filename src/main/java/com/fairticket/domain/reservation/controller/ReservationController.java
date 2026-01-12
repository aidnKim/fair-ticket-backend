package com.fairticket.domain.reservation.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fairticket.domain.reservation.dto.ReservationCreateRequestDto;
import com.fairticket.domain.reservation.dto.ReservationResponseDto;
import com.fairticket.domain.reservation.service.ReservationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 좌석 예약 (선점)
    @PostMapping
    public ResponseEntity<Long> createReservation(Principal principal,
                                                    @RequestBody ReservationCreateRequestDto requestDto) {
        Long reservationId = reservationService.createReservation(principal.getName(), requestDto);
        return ResponseEntity.ok(reservationId);
    }
    
    // 회원 별 예약 내역 조회
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponseDto>> getMyReservations(Principal principal) {
    	List<ReservationResponseDto> reservations = reservationService.getMyReservations(principal.getName());
        return ResponseEntity.ok(reservations);
    }
}