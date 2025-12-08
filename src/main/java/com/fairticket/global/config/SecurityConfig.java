package com.fairticket.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import com.fairticket.global.jwt.JwtAuthenticationFilter;
import com.fairticket.global.jwt.JwtTokenProvider;

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
                    // 1. 회원가입, 로그인은 누구나 접근 가능 (permitAll)
                    .requestMatchers("/api/v1/users/signup", "/api/v1/users/login").permitAll()
                    // 2. Swagger 문서 관련 URL도 열어두기 (나중에 쓸 거니까)
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                    // 3. 그 외의 모든 요청은 인증(토큰)이 있어야 함 (authenticated)
                    .anyRequest().authenticated()
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
}