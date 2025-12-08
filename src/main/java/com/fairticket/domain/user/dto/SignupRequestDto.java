package com.fairticket.domain.user.dto;

import com.fairticket.domain.user.model.User;
import com.fairticket.domain.user.model.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // 기본 생성자 (JSON 파싱할 때 필수)
public class SignupRequestDto {

    private String email;
    private String password;
    private String name;
    private String role; // "USER" 또는 "ADMIN" (String으로 받아서 변환할 예정)

    // DTO -> Entity 변환 메소드
    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(this.email)
                .password(encodedPassword) // 암호화된 비밀번호를 넣어야 함
                .name(this.name)
                .role(UserRole.valueOf(this.role)) // String "USER" -> Enum USER 변환
                .build();
    }
}