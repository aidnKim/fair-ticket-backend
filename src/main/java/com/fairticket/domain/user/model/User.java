package com.fairticket.domain.user.model;

import com.fairticket.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users") // 테이블명 지정
public class User extends BaseTimeEntity { // 시간 자동 관리 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // DB 컬럼명은 user_id
    private Long id;          // 자바 필드명은 id (관례상 편함)

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole role;

    @Column(nullable = false)
    private Long point;

    @Builder
    public User(String email, String password, String name, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.point = 0L; // 포인트는 생성 시 0원으로 초기화
    }
    
    // 비즈니스 로직: 포인트 충전/사용 등을 위해 메소드 추가 가능
    public void usePoint(Long amount) {
        if (this.point < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        this.point -= amount;
    }
}