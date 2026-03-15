package com.fairticket.domain.queue.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fairticket.global.kafka.UserActionEvent;
import com.fairticket.global.kafka.UserActionProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {
	
    private final RedissonClient redissonClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserActionProducer userActionProducer;
    
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
            // 이전 차단 기록 초기화 (중복 방지 Set만, count는 유지)
            redissonClient.getKeys().delete("blocked:macro:set");
            
            long baseTime = System.currentTimeMillis() - 100000;
            
            // 200개 슬롯 중 봇 30개를 랜덤 위치에 배치
            java.util.Set<Integer> botPositions = new java.util.HashSet<>();
            java.util.Random random = new java.util.Random(42);
            while (botPositions.size() < 30) {
                botPositions.add(random.nextInt(200));
            }
            
            int botIndex = 0;
            int userIndex = 0;
            for (int i = 0; i < 200; i++) {
                if (botPositions.contains(i)) {
                    queue.add(baseTime + i, "macro_" + botIndex + "@bot.com");
                    botIndex++;
                } else {
                    queue.add(baseTime + i, "user_" + userIndex + "@queue.com");
                    userIndex++;
                }
            }
            
            log.info("데모용 대기자 200명 추가됨 (봇 30 + 정상 170, 섞어 배치)");
            
            sendFakeTrafficToKafka(scheduleId);
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
            // 맨 앞 사용자 확인 (제거하지 않고 조회만)
            String email = queue.first();
            if (email == null) break;
            
            // 봇이면 처리 중단 (AI가 제거할 때까지 대기)
            if (email.endsWith("@bot.com")) {
                log.info("봇 발견, 스케줄러 대기: {}", email);
                break;
            }
            
            // 정상 사용자만 제거 & 입장 처리
            queue.pollFirst();
            redissonClient.getSet(activeKey).add(email);
            log.info("입장 허용: email={}", email);
            
            messagingTemplate.convertAndSend(
                "/topic/queue/" + scheduleId + "/" + email,
                Map.of("type", "ENTER_ALLOWED", "canEnter", true)
            );
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
    
    private void sendFakeTrafficToKafka(Long scheduleId) {
        // 별도 스레드에서 시차를 두고 전송
        new Thread(() -> {
            try {
                for (int i = 0; i < 30; i++) {
                    // 각 봇당 25건씩 burst 전송
                    for (int j = 0; j < 25; j++) {
                        UserActionEvent event = UserActionEvent.builder()
                            .userEmail("macro_" + i + "@bot.com")
                            .sessionId(null)
                            .actionType("SEAT_VIEW")
                            .ipAddress("10.0.1." + i)
                            .userAgent("MacroBot/1.0")
                            .timestamp(LocalDateTime.now())
                            .endpoint("/api/v1/concerts/" + scheduleId + "/seats")
                            .responseTimeMs(3L)
                            .build();
                        userActionProducer.sendUserAction(event);
                    }
                    Thread.sleep(1000);  // 봇 1명당 1초 간격
                }

                // 정상 사용자 170명 (AI가 통과시킴)
                for (int i = 0; i < 170; i++) {
                    UserActionEvent event = UserActionEvent.builder()
                        .userEmail("user_" + i + "@queue.com")
                        .sessionId("session_" + i)
                        .actionType("SEAT_VIEW")
                        .ipAddress("192.168." + (i / 255) + "." + (i % 255))
                        .userAgent("Mozilla/5.0")
                        .timestamp(LocalDateTime.now())
                        .endpoint("/api/v1/concerts/" + scheduleId + "/seats")
                        .responseTimeMs(200L + (long)(Math.random() * 500))
                        .build();
                    userActionProducer.sendUserAction(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("200명 트래픽 Kafka 전송 완료");
        }).start();
    }


    public void removeBlockedUser(String email) {
    	// 실제 사용자는 제거하지 않음 (봇만 제거)
        if (!email.endsWith("@bot.com")) {
            log.info("실제 사용자 보호: {}", email);
            return;
        }
        
        // 모든 스케줄의 대기열에서 해당 사용자 제거
        // (간단하게 scheduleId 1~9 범위로)
        for (long sid = 1; sid <= 9; sid++) {
            String queueKey = WAITING_QUEUE_KEY + sid;
            RScoredSortedSet<String> queue = redissonClient.getScoredSortedSet(queueKey);
            if (queue.remove(email)) {
                log.info("대기열에서 봇 제거: scheduleId={}, email={}", sid, email);
                // 순번 업데이트 브로드캐스트
                messagingTemplate.convertAndSend(
                    "/topic/queue/" + sid,
                    Map.of("type", "QUEUE_UPDATE", "remainingCount", queue.size())
                );
            }
        }
    }

}