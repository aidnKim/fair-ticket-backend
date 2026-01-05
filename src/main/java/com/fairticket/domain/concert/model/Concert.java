package com.fairticket.domain.concert.model;

import com.fairticket.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "concerts")
public class Concert extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concert_id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob // 긴 텍스트 저장용 (TEXT 타입)
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "detail_image_url", length = 500)
    private String detailImageUrl;

    // 양방향 연관관계 (Concert만 조회해도 스케줄을 알 수 있게)
    // mappedBy = "concert"는 ConcertSchedule 클래스의 concert 변수명을 뜻함
    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL)
    private List<ConcertSchedule> schedules = new ArrayList<>();

    @Builder
    public Concert(String title, String description, LocalDateTime startDate, 
    			LocalDateTime endDate, Venue venue, String imageUrl, String detailImageUrl) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.venue = venue;
        this.imageUrl = imageUrl;
        this.detailImageUrl = detailImageUrl;
    }
}