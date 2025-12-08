package com.fairticket.domain.user.dto;

import com.fairticket.domain.user.model.User;
import com.fairticket.domain.user.model.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder // 빌더 패턴 사용
public class UserResponseDto {

    private Long id;
    private String email;
    private String name;
    private UserRole role;
    private Long point;

    // Entity -> DTO 변환 메소드 (스태틱 팩토리 메소드 패턴)
    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .point(user.getPoint())
                .build();
    }
}