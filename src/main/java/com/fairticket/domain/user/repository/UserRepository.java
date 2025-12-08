package com.fairticket.domain.user.repository;

import com.fairticket.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 회원 정보 찾기 (로그인 시 필수!)
    Optional<User> findByEmail(String email);
}