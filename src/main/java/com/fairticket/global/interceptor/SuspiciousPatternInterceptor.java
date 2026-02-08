package com.fairticket.global.interceptor;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuspiciousPatternInterceptor implements HandlerInterceptor {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response, Object handler) throws Exception {
        String userAgent = request.getHeader("User-Agent");
        String ip = getClientIp(request);
        
        // 블랙리스트 확인
        if (isBlacklisted(ip)) {
            response.setStatus(429);
            return false;
        }
        
        // 1. User-Agent 검사
        if (userAgent == null || userAgent.isEmpty()) {
            blacklist(ip, "empty_ua", Duration.ofMinutes(10));
            response.setStatus(429);
            return false;
        }
        
        if (isBotUserAgent(userAgent)) {
            blacklist(ip, "bot_ua", Duration.ofMinutes(30));
            response.setStatus(429);
            return false;
        }
        
        // 2. 요청 속도 검사 (너무 빠른 연속 요청)
        if (isTooFast(ip)) {
            blacklist(ip, "speed_limit", Duration.ofMinutes(5));
            response.setStatus(429);
            return false;
        }
        
        return true;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return ip != null ? ip.split(",")[0].trim() : request.getRemoteAddr();
    }
    
    private boolean isBlacklisted(String ip) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + ip));
    }
    
    private void blacklist(String ip, String reason, Duration duration) {
        redisTemplate.opsForValue().set("blacklist:" + ip, reason, duration);
        redisTemplate.opsForValue().increment("blocked:macro:count");
        log.info("IP blacklisted: {} reason: {}", ip, reason);
    }
    
    private boolean isBotUserAgent(String ua) {
        String lower = ua.toLowerCase();
        return lower.contains("bot") || lower.contains("crawler") 
            || lower.contains("spider") || lower.contains("curl")
            || lower.contains("wget") || lower.contains("python");
    }
    
    private boolean isTooFast(String ip) {
        String key = "speed:" + ip;
        long now = System.currentTimeMillis();
        
        // 최근 요청 시간 기록
        redisTemplate.opsForList().rightPush(key, String.valueOf(now));
        redisTemplate.expire(key, Duration.ofSeconds(10));
        
        Long size = redisTemplate.opsForList().size(key);
        if (size != null && size >= 10) {
            String oldestStr = (String) redisTemplate.opsForList().index(key, 0);
            if (oldestStr != null) {
                long oldest = Long.parseLong(oldestStr);
                if (now - oldest < 1000) { // 10회가 1초 이내 = 의심
                    return true;
                }
            }
            redisTemplate.opsForList().leftPop(key);
        }
        return false;
    }
}