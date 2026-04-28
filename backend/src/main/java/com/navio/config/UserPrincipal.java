package com.navio.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * UserPrincipal
 *
 * Spring Security의 인증 주체(Principal) 구현체.
 * JwtAuthenticationFilter가 JWT에서 파싱한 userId와 email을 담아 SecurityContext에 저장한다.
 *
 * 컨트롤러에서 @AuthenticationPrincipal UserPrincipal principal 형태로 주입받는다.
 * principal.getUserId()로 현재 로그인한 사용자의 ID를 얻을 수 있다.
 *
 * 관련: JwtAuthenticationFilter, BookingController, UserController, AuthController
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String email;
    private final String role;

    public UserPrincipal(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = (role != null) ? role : "USER";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
