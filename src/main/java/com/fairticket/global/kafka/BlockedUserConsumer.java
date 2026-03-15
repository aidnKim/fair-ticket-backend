package com.fairticket.global.kafka;

import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fairticket.domain.queue.service.QueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockedUserConsumer {
    
    private static final String BLOCKED_COUNT_KEY = "blocked:macro:count";
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final QueueService queueService;
    
    @KafkaListener(topics = "blocked-users", groupId = "fraud-detection-group")
    public void handleBlockedUser(Map<String, Object> message) {
        String userIdentifier = (String) message.get("userId");
        String reason = (String) message.get("reason");
        
        // 이미 차단된 사용자는 무시 (중복 방지)
        String blockedSetKey = "blocked:macro:set";
        Boolean isNew = redisTemplate.opsForSet().add(blockedSetKey, userIdentifier) > 0;
        
        if (!isNew) {
            log.debug("이미 차단된 사용자 무시: {}", userIdentifier);
            return;
        }
        
        log.info("User blocked: userIdentifier={}, reason={}", userIdentifier, reason);
        
        // Redis에 차단 수 증가 (고유 사용자만)
        Long blockedCount = redisTemplate.opsForValue()
            .increment(BLOCKED_COUNT_KEY);
        
        // 차단된 사용자를 대기열에서도 제거
        queueService.removeBlockedUser(userIdentifier);
        
        // WebSocket으로 프론트엔드에 실시간 브로드캐스트
        messagingTemplate.convertAndSend("/topic/blocked-count", 
            Map.of("blockedCount", blockedCount, "reason", reason));
    }

    
    public Long getBlockedCount() {
        Object count = redisTemplate.opsForValue().get(BLOCKED_COUNT_KEY);
        return count != null ? Long.parseLong(count.toString()) : 0L;
    }
}