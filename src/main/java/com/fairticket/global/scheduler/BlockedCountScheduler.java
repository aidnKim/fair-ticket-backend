package com.fairticket.global.scheduler;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockedCountScheduler {

    private final RedisTemplate<String, Object> redisTemplate;

    // 매주 월요일 00:00에 리셋
    @Scheduled(cron = "0 0 0 * * MON")
    public void resetBlockedCount() {
        redisTemplate.delete("blocked:macro:count");
        redisTemplate.delete("blocked:macro:set");
        log.info("차단 카운트 주간 리셋 완료");
    }
}

