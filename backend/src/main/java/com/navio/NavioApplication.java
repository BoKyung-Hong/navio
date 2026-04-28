package com.navio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * NavioApplication
 *
 * Navio 서비스의 Spring Boot 진입점(Entry Point).
 * - @EnableScheduling: PENDING 예약 만료 스케줄러(BookingExpirationScheduler) 활성화
 *
 * 실행: ./gradlew bootRun  (로컬 프로필 자동 적용)
 */
@SpringBootApplication
@EnableScheduling
public class NavioApplication {

    public static void main(String[] args) {
        SpringApplication.run(NavioApplication.class, args);
    }
}
