package com.fairticket.global.kafka;

import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockedUserConsumer {
    
    private static final String BLOCKED_COUNT_KEY = "blocked:macro:count";
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    
    @KafkaListener(topics = "blocked-users", groupId = "fraud-detection-group")
    public void handleBlockedUser(Map<String, Object> message) {
        Long userId = (Long) message.get("userId");
        String reason = (String) message.get("reason");
        
        log.info("User blocked: userId={}, reason={}", userId, reason);
        
        // Redis에 차단 수 증가
        Long blockedCount = redisTemplate.opsForValue()
            .increment(BLOCKED_COUNT_KEY);
        
        // WebSocket으로 프론트엔드에 실시간 브로드캐스트
        messagingTemplate.convertAndSend("/topic/blocked-count", 
            Map.of("blockedCount", blockedCount, "reason", reason));
    }
    
    public Long getBlockedCount() {
        Object count = redisTemplate.opsForValue().get(BLOCKED_COUNT_KEY);
        return count != null ? Long.parseLong(count.toString()) : 0L;
    }
}