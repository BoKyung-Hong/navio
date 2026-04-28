package com.navio.domain.user.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,
        String name,
        String role,
        String phone
) {}
