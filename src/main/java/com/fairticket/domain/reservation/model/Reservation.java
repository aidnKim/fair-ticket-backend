package com.fairticket.domain.reservation.model;

import com.fairticket.domain.concert.model.ConcertSchedule;
import com.fairticket.domain.concert.model.Seat;
import com.fairticket.domain.user.model.User;
import com.fairticket.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservations")
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    // 누가 예약했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어떤 스케줄(날짜)인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ConcertSchedule schedule;

    // 어떤 좌석인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime reservationTime; // 예약 시도 시간

    // 나중에 스케줄러가 이 시간을 보고 취소시킬 예정
    @Column(nullable = false)
    private LocalDateTime expireTime;

    @Builder
    public Reservation(User user, ConcertSchedule schedule, Seat seat, LocalDateTime reservationTime) {
        this.user = user;
        this.schedule = schedule;
        this.seat = seat;
        this.status = ReservationStatus.PENDING; // 처음 생성할 땐 무조건 '결제 대기'
        this.reservationTime = reservationTime;
        // 결제 기한은 예약 시간으로부터 5분 뒤로 설정 (정책에 따라 변경 가능)
        this.expireTime = reservationTime.plusMinutes(5);
        //개발용 30초로 설정
//        this.expireTime = reservationTime.plusSeconds(30);
    }
    
    // 결제 성공 시 상태를 PAID로 변경하는 메소드 (나중에 결제 기능에서 사용)
    public void completePayment() {
        this.status = ReservationStatus.PAID;
    }
    
    // 취소 처리 메소드
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}