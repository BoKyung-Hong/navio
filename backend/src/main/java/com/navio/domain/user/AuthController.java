package com.navio.domain.user;

import com.navio.common.ApiResponse;
import com.navio.config.UserPrincipal;
import com.navio.domain.user.dto.LoginRequest;
import com.navio.domain.user.dto.RefreshRequest;
import com.navio.domain.user.dto.SignupRequest;
import com.navio.domain.user.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController
 *
 * 인증 관련 REST API 엔드포인트.
 *
 * POST /api/auth/signup   → 회원가입 (공개)
 * POST /api/auth/login    → 로그인, JWT 발급 (공개)
 * POST /api/auth/refresh  → Access Token 재발급 (공개, Refresh Token 필요)
 * POST /api/auth/logout   → 로그아웃, Redis Refresh Token 삭제 (JWT 필요)
 *
 * 관련: AuthService, JwtTokenProvider, UserPrincipal
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> signup(@Valid @RequestBody SignupRequest req) {
        Long userId = authService.signup(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(userId));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ApiResponse.ok(authService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal UserPrincipal principal) {
        authService.logout(principal.getUserId());
        return ApiResponse.ok(null);
    }
}
