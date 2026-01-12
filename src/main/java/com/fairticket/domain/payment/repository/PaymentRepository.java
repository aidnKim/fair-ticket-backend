package com.fairticket.domain.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fairticket.domain.payment.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	// reservationId로 결제 정보 조회
    Optional<Payment> findByReservationId(Long reservationId);
}