package com.navio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CorsConfig
 *
 * 프론트엔드(React, localhost:5173)와 백엔드(localhost:8080) 간의 CORS 허용 설정.
 *
 * 허용 대상: navio.cors.allowed-origins (환경변수 CORS_ALLOWED_ORIGINS로 오버라이드 가능)
 * 허용 메서드: GET, POST, PUT, PATCH, DELETE, OPTIONS
 * Authorization 헤더 노출: exposedHeaders("Authorization")
 * Credentials(쿠키/헤더) 허용: allowCredentials(true)
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${navio.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
