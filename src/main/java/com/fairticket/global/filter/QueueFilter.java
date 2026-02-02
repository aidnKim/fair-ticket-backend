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
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)  // CachingRequestFilter(0) 다음에 실행
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
        // 기본 제외 경로
        if (EXCLUDE_PATHS.stream().anyMatch(uri::startsWith)) {
            return true;
        }
        
        // 콘서트 목록 제외 (정확히 /api/v1/concerts)
        if (uri.equals("/api/v1/concerts")) {
            return true;
        }
        
        // 콘서트 상세조회 제외 (/api/v1/concerts/1 형태)
        if (uri.matches("/api/v1/concerts/\\d+$")) {
            return true;
        }
        
        return false;
    }
    
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
    
    private Long extractScheduleId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        
        // 1. /api/v1/concerts/{id}/seats 에서 추출
        Long fromUrl = extractFromUrl(uri);
        if (fromUrl != null) {
            return fromUrl;
        }
        
        // 2. POST Body에서 추출 (예약 API)
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content);
                Long fromBody = extractFromBody(body);
                if (fromBody != null) {
                    return fromBody;
                }
            }
        }
        
        return 1L;  // 기본값
    }
    private Long extractFromUrl(String uri) {
    	
        // /api/v1/concerts/3/seats → 3 추출
        if (uri.matches(".*/concerts/\\d+/seats.*")) {
            String[] parts = uri.split("/");
            for (int i = 0; i < parts.length; i++) {
                if ("concerts".equals(parts[i]) && i + 1 < parts.length) {
                    try {
                        return Long.parseLong(parts[i + 1]);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
    private Long extractFromBody(String body) {
    	
        // {"scheduleId": 1, ...} 에서 추출
        try {
            if (body.contains("scheduleId")) {
                int idx = body.indexOf("scheduleId");
                String sub = body.substring(idx);
                // 숫자만 추출
                StringBuilder num = new StringBuilder();
                boolean started = false;
                for (char c : sub.toCharArray()) {
                    if (Character.isDigit(c)) {
                        started = true;
                        num.append(c);
                    } else if (started) {
                        break;
                    }
                }
                if (num.length() > 0) {
                    return Long.parseLong(num.toString());
                }
            }
        } catch (Exception e) {
            // 파싱 실패 시 무시
        }
        return null;
    }
}
