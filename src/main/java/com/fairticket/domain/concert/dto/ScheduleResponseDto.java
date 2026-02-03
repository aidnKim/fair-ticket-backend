package com.fairticket.domain.concert.dto;

import java.time.LocalDateTime;

import com.fairticket.domain.concert.model.ConcertSchedule;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponseDto {
	private Long id;
    private LocalDateTime concertDate;
    private int totalSeats;
    private int availableSeats;
    
    public static ScheduleResponseDto from(ConcertSchedule schedule, int availableSeats) {
    		return ScheduleResponseDto.builder()
    				.id(schedule.getId())
    				.concertDate(schedule.getConcertDate())
    				.totalSeats(schedule.getTotalSeats())
    				.availableSeats(availableSeats)  // Redis에서 가져온 값
    				.build();
    }

}
