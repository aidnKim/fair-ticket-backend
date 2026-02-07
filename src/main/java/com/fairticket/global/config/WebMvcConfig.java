package com.fairticket.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fairticket.global.interceptor.UserActionInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final UserActionInterceptor userActionInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userActionInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/auth/**", "/api/health");
    }
}

