package com.navio.config;

import com.navio.common.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter
 *
 * 모든 HTTP 요청에서 Authorization 헤더의 Bearer 토큰을 검증하고
 * SecurityContext에 인증 정보(UserPrincipal)를 설정하는 필터.
 *
 * 처리 흐름:
 *   1. Authorization: Bearer {token} 헤더 추출
 *   2. JwtTokenProvider.validateToken() 으로 서명·만료 검증
 *   3. userId/email 파싱 → UserPrincipal 생성
 *   4. SecurityContextHolder에 UsernamePasswordAuthenticationToken 등록
 *   5. 토큰 오류 시 SecurityContext 초기화 → SecurityConfig의 .authenticated() 에서 401 반환
 *
 * 등록 위치: SecurityConfig.addFilterBefore(this, UsernamePasswordAuthenticationFilter.class)
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            try {
                jwtTokenProvider.validateToken(token);
                Long userId = jwtTokenProvider.getUserId(token);
                String email = jwtTokenProvider.getEmail(token);
                String role = jwtTokenProvider.getRole(token);
                UserPrincipal principal = new UserPrincipal(userId, email, role);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (BusinessException e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
