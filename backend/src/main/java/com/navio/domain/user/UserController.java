package com.navio.domain.user;

import com.navio.common.ApiResponse;
import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import com.navio.config.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * UserController
 *
 * 현재 로그인한 사용자의 정보 조회 및 수정 API.
 *
 * GET  /api/users/me   → 내 정보 조회 (JWT 필요)
 * PATCH /api/users/me  → 이름, 전화번호 수정 (JWT 필요)
 *
 * userId는 JwtAuthenticationFilter가 SecurityContext에 세팅한 UserPrincipal에서 가져온다.
 * 따라서 URL 경로에 userId를 노출하지 않아도 된다.
 *
 * 관련: User, UserRepository, UserPrincipal
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = findUser(principal.getUserId());
        return ApiResponse.ok(toMap(user));
    }

    @PatchMapping("/me")
    public ApiResponse<Map<String, Object>> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body
    ) {
        User user = findUser(principal.getUserId());
        user.update(body.get("name"), body.get("phone"));
        userRepository.save(user);
        return ApiResponse.ok(toMap(user));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    private Map<String, Object> toMap(User user) {
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "phone", user.getPhone() != null ? user.getPhone() : ""
        );
    }
}
