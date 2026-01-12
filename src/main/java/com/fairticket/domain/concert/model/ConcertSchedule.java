package com.fairticket.domain.concert.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import com.fairticket.global.common.BaseTimeEntity;

@Slf4j
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "concert_schedules")
public class ConcertSchedule extends BaseTimeEntity {

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
            log.warn("availableSeats 불일치 감지! scheduleId={}", this.id);
            return;  // 예약은 막지 않음
        }
        this.availableSeats--;
    }
    
    // 예매 취소 시 잔여 좌석 증가 로직
    public void increaseAvailableSeats() {
    	if (this.availableSeats >= this.totalSeats) {
            // 로그만 남기고 넘어감 (취소는 막지 않음)
            log.warn("잔여좌석이 이미 최대입니다. scheduleId={}, availableSeats={}, totalSeats={}", 
                     this.id, this.availableSeats, this.totalSeats);
            return;
        }
        this.availableSeats++;
    }
}