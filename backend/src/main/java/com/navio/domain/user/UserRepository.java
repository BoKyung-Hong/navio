package com.navio.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository
 *
 * User 엔티티의 JPA 레포지토리.
 *
 * findByEmail    : 로그인 시 이메일로 사용자 조회
 * existsByEmail  : 회원가입 시 이메일 중복 확인
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
