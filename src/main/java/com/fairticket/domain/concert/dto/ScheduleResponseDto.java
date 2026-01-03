package com.fairticket.domain.concert.dto;

import java.time.LocalDateTime;

import com.fairticket.domain.concert.model.ConcertSchedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ScheduleResponseDto {
	private Long id;
    private LocalDateTime concertDate;
    private int totalSeats;
    private int availableSeats;
    
    public static ScheduleResponseDto from(ConcertSchedule schedule) {
    		return ScheduleResponseDto.builder()
    				.id(schedule.getId())
    				.concertDate(schedule.getConcertDate())
    				.totalSeats(schedule.getTotalSeats())
    				.availableSeats(schedule.getAvailableSeats())
    				.build();
    }

}
