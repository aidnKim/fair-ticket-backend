package com.fairticket.global.interceptor;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fairticket.global.kafka.UserActionEvent;
import com.fairticket.global.kafka.UserActionProducer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserActionInterceptor implements HandlerInterceptor {
    
    private final UserActionProducer userActionProducer;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                             Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long responseTime = System.currentTimeMillis() - startTime;
        
        // userId는 SecurityContext에서 가져오거나 세션에서 추출
        String userEmail = extractUserEmail(request);
        
        UserActionEvent event = UserActionEvent.builder()
            .userEmail(userEmail)
            .sessionId(getSessionId(request))
            .actionType(determineActionType(request))
            .ipAddress(getClientIp(request))
            .userAgent(request.getHeader("User-Agent"))
            .timestamp(LocalDateTime.now())
            .endpoint(request.getRequestURI())
            .responseTimeMs(responseTime)
            .build();
        
        userActionProducer.sendUserAction(event);
    }
    
    private String extractUserEmail(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() 
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
    }
    
    private String determineActionType(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("/seats")) return "SEAT_VIEW";
        if (uri.contains("/reservations")) return "RESERVATION_ATTEMPT";
        return "PAGE_LOAD";
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return ip != null ? ip.split(",")[0] : request.getRemoteAddr();
    }
    
    private String getSessionId(HttpServletRequest request) {
        try {
            return request.getSession(false) != null ? request.getSession(false).getId() : null;
        } catch (IllegalStateException e) {
            return null;
        }
    }
}

