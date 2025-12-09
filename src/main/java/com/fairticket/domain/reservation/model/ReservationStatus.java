package com.fairticket.domain.reservation.model;

public enum ReservationStatus {
    PENDING,   // 예약 중 (결제 대기 - 임시 점유)
    PAID,      // 결제 완료 (최종 확정)
    CANCELLED  // 취소됨 (시간 초과 등)
}