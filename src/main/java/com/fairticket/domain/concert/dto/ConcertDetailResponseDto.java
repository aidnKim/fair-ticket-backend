package com.fairticket.domain.concert.dto;

import com.fairticket.domain.concert.model.Concert;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
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

    public ConcertDetailResponseDto(Concert concert) {
        this.id = concert.getId();
        this.title = concert.getTitle();
        this.description = concert.getDescription();
        this.venue = concert.getVenue() != null ? concert.getVenue().getName() : null;
        this.imageUrl = concert.getImageUrl();
        this.detailImageUrl = concert.getDetailImageUrl();
        this.startDate = concert.getStartDate();
        this.endDate = concert.getEndDate();
        // 스케줄 엔티티 -> DTO 변환
        this.schedules = concert.getSchedules().stream()
                .map(ScheduleResponseDto::from)
                .collect(Collectors.toList());
    }


}