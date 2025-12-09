package com.fairticket.domain.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationCreateRequestDto {
    private Long scheduleId;
    private Long seatId;
}