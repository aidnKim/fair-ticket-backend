package com.fairticket.domain.queue.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {
	
    private final RedissonClient redissonClient;
    
    private static final String WAITING_QUEUE_KEY = "waiting_queue:";
    private static final String ACTIVE_SET_KEY = "active_set:";
    
    // 대기열 등록
    public Long enterQueue(Long scheduleId, String email) {
        String queueKey = WAITING_QUEUE_KEY + scheduleId;
        RScoredSortedSet<String> queue = redissonClient.getScoredSortedSet(queueKey);
        
        if (queue.contains(email)) {
            return getQueuePosition(scheduleId, email);
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
            }
        }
        redissonClient.getSet(activeKey).expire(java.time.Duration.ofMinutes(10));
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