package com.fairticket.domain.concert.dto;

import java.time.LocalDateTime;

import com.fairticket.domain.concert.model.Concert;
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
public class ConcertResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String imageUrl;
    private String venue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ConcertResponseDto from(Concert concert) {
        return ConcertResponseDto.builder()
                .id(concert.getId())
                .title(concert.getTitle())
                .description(concert.getDescription())
                .startDate(concert.getStartDate())
                .endDate(concert.getEndDate())
                .imageUrl(concert.getImageUrl())
                .venue(concert.getVenue() != null ? concert.getVenue().getName() : null)
                .createdAt(concert.getCreatedAt())
                .updatedAt(concert.getUpdatedAt())
                .build();
    }
}