package com.fairticket.domain.payment.model;

import com.fairticket.domain.reservation.model.Reservation;
import com.fairticket.domain.user.model.User;
import com.fairticket.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    // 어떤 예약을 결제했는지 (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // 누가 결제했는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Builder
    public Payment(Reservation reservation, User user, BigDecimal amount, PaymentStatus status) {
        this.reservation = reservation;
        this.user = user;
        this.amount = amount;
        this.status = status;
    }
}