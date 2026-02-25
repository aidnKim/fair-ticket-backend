package com.fairticket.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fairticket.global.interceptor.RateLimitInterceptor;
import com.fairticket.global.interceptor.SuspiciousPatternInterceptor;
import com.fairticket.global.interceptor.UserActionInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final RateLimitInterceptor rateLimitInterceptor;
    private final SuspiciousPatternInterceptor suspiciousPatternInterceptor;
    private final UserActionInterceptor userActionInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 순서: 1차 차단 → 로깅
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/v1/queue/**", "/api/v1/users/**", "/api/v1/admin/**")
            .order(1);
            
        registry.addInterceptor(suspiciousPatternInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/v1/queue/**", "/api/v1/users/**", "/api/v1/admin/**")
            .order(2);
            
        registry.addInterceptor(userActionInterceptor)
	        .addPathPatterns("/api/**")
	        .excludePathPatterns("/api/v1/users/login", "/api/v1/users/signup", "/api/v1/admin/**")
	        .order(3);

    }
}