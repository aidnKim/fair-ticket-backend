package com.fairticket.domain.queue.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {
	
    private final RedissonClient redissonClient;
    private final SimpMessagingTemplate messagingTemplate;
    
    private static final String WAITING_QUEUE_KEY = "waiting_queue:";
    private static final String ACTIVE_SET_KEY = "active_set:";
    
    // 대기열 등록
    public Long enterQueue(Long scheduleId, String email) {
    	
    	// 이전 active 상태 정리 (데모용)
    	redissonClient.getSet(ACTIVE_SET_KEY + scheduleId).remove(email);
    	
        String queueKey = WAITING_QUEUE_KEY + scheduleId;
        RScoredSortedSet<String> queue = redissonClient.getScoredSortedSet(queueKey);
        
        if (queue.contains(email)) {
            return getQueuePosition(scheduleId, email);
        }
        
        // 대기열이 비어있으면 가짜 대기자 추가 (데모용)
        if (queue.isEmpty()) {
            long baseTime = System.currentTimeMillis() - 100000;
            for (int i = 0; i < 50; i++) {
                queue.add(baseTime + i, "fake_user_" + i + "@bot.com");
            }
            log.info("데모용 가짜 대기자 50명 추가됨");
        }
        
        queue.add(System.currentTimeMillis(), email);
        log.info("대기열 등록: scheduleId={}, email={}", scheduleId, email);
        return getQueuePosition(scheduleId, email);
    }
    
    // 순번 조회 (1부터 시작)
    public Long getQueuePosition(Long scheduleId, String email) {
        String queueKey = WAITING_QUEUE_KEY + scheduleId;
        RScoredSortedSet<String> queue = redissonClient.getScoredSortedSet(queueKey);
        
        Integer rank = queue.rank(email);
        return (rank == null) ? -1L : (long) (rank + 1);
    }
    
    // 입장 가능 여부
    public boolean canEnter(Long scheduleId, String email) {
        return redissonClient.getSet(ACTIVE_SET_KEY + scheduleId).contains(email);
    }
    
    // 다음 N명 입장 허용
    public void allowNextUsers(Long scheduleId, int count) {
        String queueKey = WAITING_QUEUE_KEY + scheduleId;
        String activeKey = ACTIVE_SET_KEY + scheduleId;
        
        RScoredSortedSet<String> queue = redissonClient.getScoredSortedSet(queueKey);
        
        for (int i = 0; i < count && !queue.isEmpty(); i++) {
            String email = queue.pollFirst();
            if (email != null) {
                redissonClient.getSet(activeKey).add(email);
                log.info("입장 허용: email={}", email);
                
                // WebSocket으로 개인 알림 (입장 가능)
                messagingTemplate.convertAndSend(
                    "/topic/queue/" + scheduleId + "/" + email,
                    Map.of("type", "ENTER_ALLOWED", "canEnter", true)
                );
            }
        }
        redissonClient.getSet(activeKey).expire(java.time.Duration.ofMinutes(10));
        
        // 전체 대기자에게 순번 업데이트 브로드캐스트
        messagingTemplate.convertAndSend(
            "/topic/queue/" + scheduleId,
            Map.of("type", "QUEUE_UPDATE", "remainingCount", queue.size())
        );
    }
    
    // 대기 인원
    public int getQueueSize(Long scheduleId) {
        return redissonClient.getScoredSortedSet(WAITING_QUEUE_KEY + scheduleId).size();
    }
    
    // 대기열 이탈
    public void leaveQueue(Long scheduleId, String email) {
        redissonClient.getScoredSortedSet(WAITING_QUEUE_KEY + scheduleId).remove(email);
        redissonClient.getSet(ACTIVE_SET_KEY + scheduleId).remove(email);
    }
}