package com.fairticket.domain.concert.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleCreateRequestDto {
    private LocalDateTime concertDate; // 공연 날짜 및 시간
    private int totalSeats;            // 생성할 총 좌석 수 (예: 50개)
}