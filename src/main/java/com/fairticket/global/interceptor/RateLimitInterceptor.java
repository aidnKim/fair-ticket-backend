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
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final int MAX_REQUESTS = 60;
    private static final int WINDOW_SECONDS = 60;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response, Object handler) throws Exception {
        String ip = getClientIp(request);
        
        // Redis에 블랙리스트 확인
        if (isBlacklisted(ip)) {
            log.warn("Blocked IP attempt: {}", ip);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"잠시 후 다시 시도해주세요.\"}");
            return false;
        }
        
        String key = "rate:" + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }
        
        if (count > MAX_REQUESTS) {
            blacklist(ip, "rate_limit", Duration.ofMinutes(5));
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Rate Limit Exceeded\", \"message\": \"요청이 너무 많습니다.\"}");
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
}