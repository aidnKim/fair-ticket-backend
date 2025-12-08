package com.fairticket.domain.concert.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long id;

    // 어느 일정(Schedule)의 좌석인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ConcertSchedule schedule;

    @Column(nullable = false)
    private int seatNo; // 좌석 번호 (1, 2, 3...)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatGrade grade; // VIP, R, S

    @Column(nullable = false)
    private BigDecimal price; // 가격 (소수점 계산 정확도를 위해 BigDecimal 사용)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status; // AVAILABLE, SOLD

    @Version // ★ 중요: 낙관적 락(Optimistic Lock)을 위한 버전 관리
    private Long version;

    @Builder
    public Seat(ConcertSchedule schedule, int seatNo, SeatGrade grade, BigDecimal price, SeatStatus status) {
        this.schedule = schedule;
        this.seatNo = seatNo;
        this.grade = grade;
        this.price = price;
        this.status = status;
    }
    
    // 좌석 상태 변경 로직
    public void reserve() {
        if (this.status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }
        this.status = SeatStatus.TEMPORARY_RESERVED;
    }
}