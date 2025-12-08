package com.fairticket.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 상속받는 자식 클래스에게 매핑 정보(컬럼)만 제공 ("나는 진짜 테이블은 아니지만, 나를 상속받는 애들(User)한테 내 컬럼(created_at)을 물려줄게.")
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 포함 ("스프링 시큐리티 CCTV(리스너)를 달아서, 데이터가 변할 때마다 감시할게.")
public abstract class BaseTimeEntity {

    @CreatedDate // ("데이터가 처음 생길 때(Insert), 현재 시간을 자동으로 넣어!")
    @Column(name = "created_at", updatable = false) // 생성 시간은 변경 불가능
    private LocalDateTime createdAt;

    @LastModifiedDate // ("데이터가 조금이라도 수정될 때(Update), 현재 시간으로 덮어씌워!")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}