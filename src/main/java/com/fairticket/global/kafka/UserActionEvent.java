package com.fairticket.global.kafka;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActionEvent {
	private String userEmail;
    private String sessionId;
    private String actionType;      // "SEAT_VIEW", "RESERVATION_ATTEMPT", "PAGE_LOAD"
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String endpoint;
    private Long responseTimeMs;    // 요청 응답 시간
    private boolean isSuspicious;   // AI가 판단할 플래그
}