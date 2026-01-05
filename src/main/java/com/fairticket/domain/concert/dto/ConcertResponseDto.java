package com.fairticket.domain.concert.dto;

import com.fairticket.domain.concert.model.Concert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
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