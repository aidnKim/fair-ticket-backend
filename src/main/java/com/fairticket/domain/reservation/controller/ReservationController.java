package com.fairticket.domain.reservation.controller;

import com.fairticket.domain.reservation.dto.ReservationCreateRequestDto;
import com.fairticket.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

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
}