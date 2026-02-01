package com.fairticket.global.filter;

import com.fairticket.domain.queue.service.QueueService;
import com.fairticket.global.jwt.JwtTokenProvider;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)  // 다른 필터보다 먼저 실행
public class QueueFilter implements Filter {
	
    private final QueueService queueService;
    private final JwtTokenProvider jwtTokenProvider;
    
    // 대기열 체크 제외할 경로들
    private static final List<String> EXCLUDE_PATHS = List.of(
        "/api/v1/queue",      // 대기열 API 자체
        "/api/v1/users",       // 로그인/회원가입
        "/ws",                // WebSocket
        "/swagger",           // Swagger
        "/h2-console"         // H2 콘솔
    );
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String uri = httpRequest.getRequestURI();
        
        // 1. 제외 경로는 바로 통과
        if (isExcludedPath(uri)) {
            chain.doFilter(request, response);
            return;
        }
        
        // 2. JWT 토큰에서 이메일 추출
        String token = resolveToken(httpRequest);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            // 토큰 없으면 인증 필터에서 처리하도록 통과
            chain.doFilter(request, response);
            return;
        }
        String email = jwtTokenProvider.getEmail(token);
        
        // 3. 대기열 통과 여부 확인
        // scheduleId는 요청에서 추출 (없으면 기본값 1L 사용 - 테스트용)
        Long scheduleId = extractScheduleId(httpRequest);
        
        if (!queueService.canEnter(scheduleId, email)) {
            // 대기열 미통과 → 429 응답
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write(
                "{\"error\": \"대기열에서 기다려주세요.\", \"message\": \"대기열 통과 후 이용 가능합니다.\"}"
            );
            return;
        }
        
        // 4. 통과 → 다음 필터로
        chain.doFilter(request, response);
    }
    
    private boolean isExcludedPath(String uri) {
        return EXCLUDE_PATHS.stream().anyMatch(uri::startsWith);
    }
    
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
    
    private Long extractScheduleId(HttpServletRequest request) {
        // URL에서 scheduleId 추출 시도
        // 예: /api/v1/reservations → body에서, /api/v1/concerts/1/seats → path에서
        String uri = request.getRequestURI();
        
        // 간단한 구현: 기본값 1L (실제로는 요청에 맞게 수정 필요)
        // 또는 모든 스케줄에 대해 하나의 대기열 사용
        return 1L;
    }
}
