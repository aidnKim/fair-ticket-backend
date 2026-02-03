package com.fairticket.domain.concert.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fairticket.domain.concert.model.Concert;
import com.fairticket.domain.concert.service.SeatAvailabilityService;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ConcertDetailResponseDto {
    private Long id;
    private String title;
    private String description;
    private String venue;
    private String imageUrl;
    private String detailImageUrl; // 상세 이미지
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<ScheduleResponseDto> schedules; // 예매 가능 스케줄 목록

    public ConcertDetailResponseDto(Concert concert, SeatAvailabilityService availabilityService) {
        this.id = concert.getId();
        this.title = concert.getTitle();
        this.description = concert.getDescription();
        this.venue = concert.getVenue() != null ? concert.getVenue().getName() : null;
        this.imageUrl = concert.getImageUrl();
        this.detailImageUrl = concert.getDetailImageUrl();
        this.startDate = concert.getStartDate();
        this.endDate = concert.getEndDate();
        // 스케줄에 실시간 잔여석 반영
        this.schedules = concert.getSchedules().stream()
                .map(s -> ScheduleResponseDto.from(s, 
                        availabilityService.getAvailableSeats(s.getId())))
                .collect(Collectors.toList());
    }


}