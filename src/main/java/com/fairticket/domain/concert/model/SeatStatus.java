package com.fairticket.domain.concert.model;

public enum SeatStatus {
    AVAILABLE,          // 예약 가능
    TEMPORARY_RESERVED, // 예약 진행 중 (임시 배정)
    SOLD                // 판매 완료
}