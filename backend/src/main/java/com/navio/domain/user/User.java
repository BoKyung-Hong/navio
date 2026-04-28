package com.navio.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User (엔티티)
 *
 * 회원 정보를 저장하는 JPA 엔티티. DB 테이블명: users
 *
 * 필드:
 *   - id        : PK (AUTO_INCREMENT)
 *   - email     : 로그인 ID, 유니크 (최대 100자)
 *   - password  : BCrypt 해시 저장 (평문 저장 금지)
 *   - name      : 표시 이름 (최대 50자)
 *   - phone     : 전화번호 (선택, 최대 20자)
 *   - role      : "USER" | "ADMIN" (기본값 USER)
 *   - createdAt / updatedAt : @PrePersist / @PreUpdate 자동 관리
 *
 * 생성 패턴: User.signup() 팩토리 메서드 사용 (Builder 직접 사용 지양)
 *
 * 관련: UserRepository, AuthService, UserController
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.role == null) this.role = "USER";
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** 회원가입용 팩토리 메서드. password는 반드시 BCrypt 인코딩 후 전달할 것. */
    public static User signup(String email, String encodedPassword, String name, String phone) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .phone(phone)
                .role("USER")
                .build();
    }

    /** 내 정보 수정. null·공백인 값은 무시하고 기존 값 유지. */
    public void update(String name, String phone) {
        if (name != null && !name.isBlank()) this.name = name;
        if (phone != null && !phone.isBlank()) this.phone = phone;
    }
}
