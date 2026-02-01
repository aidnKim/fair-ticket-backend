package com.fairticket.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fairticket.global.jwt.JwtAuthenticationFilter;
import com.fairticket.global.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    	httpSecurity
            // 1. 보안 설정 초기화 (CSRF 끄기, REST API이므로 세션 안 씀(STATELESS) 설정)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 2. 권한 설정 (개발 편의를 위해 일단 모두 허용)
            .authorizeHttpRequests(auth -> auth
                    // 회원가입, 로그인은 누구나 접근 가능
                    .requestMatchers("/api/v1/users/signup", "/api/v1/users/login").permitAll()
                    // Swagger 문서 관련 URL도 열어두기
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                    // web socket 접근 허용 
                    .requestMatchers("/ws/**").permitAll()
                    // 추가: 공연 조회는 공개
                    .requestMatchers(HttpMethod.GET, "/api/v1/concerts/**").permitAll()
                    // 그 외의 모든 요청은 인증(토큰)이 있어야 함 (authenticated)
                    .anyRequest().authenticated()
            )
            // 인증 실패 시 401 반환
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\": \"인증이 필요합니다.\"}");
                })
            )
            
            // 3. JWT 필터 끼워넣기 (ID/PW 검사 전에 돌도록)
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    // 비밀번호 암호화 도구
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // AuthenticationManager를 Bean으로 등록 (Controller에서 쓰기 위해)
    // 수동으로 연결 안 해도, 스프링이 알아서 UserDetailsService와 PasswordEncoder를 찾아서 연결해 줌
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins(
                    		"http://localhost:5173",
                    		"https://fairticket.store",
                    	    "https://www.fairticket.store"
                    		) // 리액트 주소 허용
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
        }
    }
}