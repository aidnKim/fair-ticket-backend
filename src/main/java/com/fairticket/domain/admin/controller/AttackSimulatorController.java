package com.fairticket.domain.admin.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fairticket.global.kafka.UserActionEvent;
import com.fairticket.global.kafka.UserActionProducer;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AttackSimulatorController {

    private final UserActionProducer userActionProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/simulate-attack")
    public Map<String, Object> simulateAttack() {
        String fakeBotId = "macro_bot_" + System.currentTimeMillis();

        // 25개 가짜 요청 생성 (AI의 burst 패턴 탐지 트리거)
        for (int i = 0; i < 25; i++) {
            UserActionEvent fakeEvent = UserActionEvent.builder()
                    .userEmail(fakeBotId)
                    .sessionId(null) // session_hopping 트리거
                    .actionType("SEAT_VIEW")
                    .ipAddress("192.168.99.99") // 고정 IP로 burst 탐지
                    .userAgent("MacroBot/1.0")
                    .timestamp(LocalDateTime.now())
                    .endpoint("/api/v1/concerts/1/seats")
                    .responseTimeMs(5L) // 비정상적으로 빠름
                    .build();

            userActionProducer.sendUserAction(fakeEvent);
        }

        return Map.of(
                "success", true,
                "message", "25개 가짜 요청이 AI 서버로 전송됨",
                "botId", fakeBotId);
    }
    
    @GetMapping("/blocked-count")
    public Map<String, Object> getBlockedCount() {
        Object count = redisTemplate.opsForValue().get("blocked:macro:count");
        Long blockedCount = count != null ? Long.parseLong(count.toString()) : 0L;
        return Map.of("blockedCount", blockedCount);
    }
}