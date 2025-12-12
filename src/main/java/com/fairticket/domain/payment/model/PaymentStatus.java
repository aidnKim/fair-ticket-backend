package com.fairticket.domain.payment.model;

public enum PaymentStatus {
    COMPLETED, // 결제 성공
    FAILED,    // 결제 실패
    REFUNDED   // 환불됨
}