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
 * 현재 로그인한 사용자의 정보 조회·수정·비밀번호 변경·탈퇴 API.
 *
 * GET    /api/users/me           → 내 정보 조회
 * PATCH  /api/users/me          → 이름, 전화번호 수정
 * PATCH  /api/users/me/password → 비밀번호 변경 (currentPassword, newPassword 필요)
 * DELETE /api/users/me          → 회원 탈퇴 (password 필요, 예약·결제 데이터 함께 삭제)
 *
 * 관련: User, UserRepository, UserService, UserPrincipal
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

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

    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body
    ) {
        String current = body.get("currentPassword");
        String next = body.get("newPassword");
        if (current == null || next == null || next.length() < 8) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        userService.changePassword(principal.getUserId(), current, next);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteAccount(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body
    ) {
        String password = body.get("password");
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        userService.deleteAccount(principal.getUserId(), password);
        return ApiResponse.ok(null);
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
