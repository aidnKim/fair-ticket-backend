package com.fairticket.domain.user.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fairticket.domain.user.dto.LoginRequestDto;
import com.fairticket.domain.user.dto.SignupRequestDto;
import com.fairticket.domain.user.dto.UserResponseDto;
import com.fairticket.domain.user.model.User;
import com.fairticket.domain.user.repository.UserRepository;
import com.fairticket.global.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정 (성능 최적화)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @Transactional // 쓰기 작업이 있으므로 Transactional 필요
    public Long signup(SignupRequestDto requestDto) {
        // 1. 이메일 중복 검사
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 3. DTO -> Entity 변환 및 저장
        User user = requestDto.toEntity(encodedPassword);
        return userRepository.save(user).getId();
    }

    /**
     * 로그인 (JWT 토큰 반환)
     */
    @Transactional
    public String login(LoginRequestDto requestDto) {
        // 1. 인증 토큰 생성 (아직 검증 전)
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword());

        // 2. 실제 검증 (여기서 비밀번호 체크가 자동으로 일어남!)
        // AuthenticationManager가 CustomUserDetailsService를 호출해서 DB 확인 및 비번 대조를 수행함.
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 검증 통과 시 JWT 토큰 생성 및 반환
        return jwtTokenProvider.createToken(authentication);
    }
    
    /**
     * 내 정보 조회
     */
    public UserResponseDto getMyInfo(String email) {
    	// [디버깅용 로그] 서비스에 넘어온 이메일 확인!
    	log.debug("🔑 [Controller] 요청한 유저 이메일: {}", email);
    	
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        
        // Entity를 DTO로 변환해서 반환
        return UserResponseDto.from(user);
    }
}