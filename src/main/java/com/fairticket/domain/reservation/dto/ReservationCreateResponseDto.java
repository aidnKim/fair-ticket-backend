package com.fairticket.domain.reservation.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationCreateResponseDto {
    private Long reservationId;
    private LocalDateTime expireTime;
}