package com.navio.domain.user;

import com.navio.common.ApiResponse;
import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AdminUserController
 *
 * 관리자 전용 사용자 관리 API. Spring Security로 ROLE_ADMIN 필수.
 *
 * GET   /api/admin/users             → 전체 사용자 목록 (id, email, name, phone, role, createdAt)
 * PATCH /api/admin/users/{id}/role  → 사용자 역할 변경 (USER ↔ ADMIN)
 *
 * 관련: User, UserRepository, SecurityConfig
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listUsers() {
        List<Map<String, Object>> result = userRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "email", u.getEmail(),
                        "name", u.getName(),
                        "phone", u.getPhone() != null ? u.getPhone() : "",
                        "role", u.getRole(),
                        "createdAt", u.getCreatedAt().toString()
                ))
                .toList();
        return ApiResponse.ok(result);
    }

    @PatchMapping("/{id}/role")
    public ApiResponse<Void> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String newRole = body.get("role");
        if (!"USER".equals(newRole) && !"ADMIN".equals(newRole)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        user.changeRole(newRole);
        userRepository.save(user);
        return ApiResponse.ok(null);
    }
}
