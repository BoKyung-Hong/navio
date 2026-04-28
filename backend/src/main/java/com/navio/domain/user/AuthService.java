package com.navio.domain.user;

import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import com.navio.config.JwtTokenProvider;
import com.navio.domain.user.dto.LoginRequest;
import com.navio.domain.user.dto.SignupRequest;
import com.navio.domain.user.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService
 *
 * 회원가입, 로그인, 토큰 갱신, 로그아웃 비즈니스 로직.
 *
 * 주요 메서드:
 *   - signup()  : 이메일 중복 확인 → BCrypt 해싱 → User 저장
 *   - login()   : 이메일/비밀번호 검증 → JWT Access/Refresh Token 발급
 *   - refresh() : Refresh Token 검증(서명 + Redis 일치) → 새 토큰 쌍 발급
 *   - logout()  : Redis에서 Refresh Token 삭제 (이후 refresh 요청 무효화)
 *
 * 의존: UserRepository, PasswordEncoder, JwtTokenProvider
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = User.signup(
                req.email(),
                passwordEncoder.encode(req.password()),
                req.name(),
                req.phone()
        );
        return userRepository.save(user).getId();
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return new TokenResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getName());
    }

    public TokenResponse refresh(String refreshToken) {
        jwtTokenProvider.validateToken(refreshToken);
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        if (!jwtTokenProvider.isRefreshTokenValid(userId, refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return new TokenResponse(newAccessToken, newRefreshToken, user.getId(), user.getEmail(), user.getName());
    }

    public void logout(Long userId) {
        jwtTokenProvider.deleteRefreshToken(userId);
    }
}
