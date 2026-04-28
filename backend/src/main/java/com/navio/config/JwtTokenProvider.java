package com.navio.config;

import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JwtTokenProvider
 *
 * JWT Access Token / Refresh Token 생성 · 검증 · Redis 관리를 담당하는 컴포넌트.
 *
 * Access Token:
 *   - 유효기간: navio.jwt.access-expiry-ms (기본 30분)
 *   - Claim: subject=userId, email
 *
 * Refresh Token:
 *   - 유효기간: navio.jwt.refresh-expiry-ms (기본 7일)
 *   - Redis 키: "refresh:{userId}" 로 저장
 *   - 로그아웃 시 Redis에서 삭제 → 토큰 무효화
 *
 * 의존: JJWT 0.12.x, Spring Data Redis (StringRedisTemplate)
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpiryMs;
    private final long refreshExpiryMs;
    private final StringRedisTemplate redis;

    private static final String REFRESH_PREFIX = "refresh:";

    public JwtTokenProvider(
            @Value("${navio.jwt.secret}") String secret,
            @Value("${navio.jwt.access-expiry-ms}") long accessExpiryMs,
            @Value("${navio.jwt.refresh-expiry-ms}") long refreshExpiryMs,
            StringRedisTemplate redis
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiryMs = accessExpiryMs;
        this.refreshExpiryMs = refreshExpiryMs;
        this.redis = redis;
    }

    /** Access Token 생성. 컨트롤러에서 @AuthenticationPrincipal로 주입될 userId/email/role을 포함한다. */
    public String generateAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role != null ? role : "USER")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessExpiryMs))
                .signWith(key)
                .compact();
    }

    public String getRole(String token) {
        String role = parseClaims(token).get("role", String.class);
        return role != null ? role : "USER";
    }

    /** Refresh Token 생성 후 Redis에 저장. 기존 토큰은 덮어쓴다. */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpiryMs))
                .signWith(key)
                .compact();
        redis.opsForValue().set(REFRESH_PREFIX + userId, token, refreshExpiryMs, TimeUnit.MILLISECONDS);
        return token;
    }

    /** 토큰 서명 및 만료 검증. 만료 시 TOKEN_EXPIRED, 위변조 시 TOKEN_INVALID 예외. */
    public void validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    /** Redis에 저장된 Refresh Token과 일치하는지 검증 (토큰 재사용 공격 방지). */
    public boolean isRefreshTokenValid(Long userId, String token) {
        String stored = redis.opsForValue().get(REFRESH_PREFIX + userId);
        return token.equals(stored);
    }

    /** 로그아웃 시 Redis에서 Refresh Token 삭제. */
    public void deleteRefreshToken(Long userId) {
        redis.delete(REFRESH_PREFIX + userId);
    }

    /** 만료된 토큰에서도 Claim을 읽을 수 있도록 ExpiredJwtException의 claims를 반환. */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
