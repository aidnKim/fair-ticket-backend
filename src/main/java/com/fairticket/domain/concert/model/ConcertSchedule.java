package com.fairticket.domain.concert.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "concert_schedules")
public class ConcertSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    // Concert와 연결 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @Column(nullable = false)
    private LocalDateTime concertDate; // 실제 공연 시작 시간

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private int availableSeats;

    @Builder
    public ConcertSchedule(Concert concert, LocalDateTime concertDate, int totalSeats) {
        this.concert = concert;
        this.concertDate = concertDate;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats; // 처음엔 전체 좌석이 곧 예매 가능 좌석
    }
    
    // 예매 시 잔여 좌석 감소 로직
    public void decreaseAvailableSeats() {
        if (this.availableSeats <= 0) {
            throw new IllegalStateException("잔여 좌석이 없습니다.");
        }
        this.availableSeats--;
    }
}