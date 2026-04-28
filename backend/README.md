# Navio Backend

Spring Boot 3 + Java 17 + JPA + Spring Security + Redis.

## 실행 (로컬)

```bash
# 1. 루트에서 MySQL/Redis 컨테이너 실행
cd ..
docker compose up -d

# 2. 백엔드 실행
cd backend
./gradlew bootRun
```

기본 포트: `http://localhost:8080`

## Health Check
```
GET http://localhost:8080/actuator/health
```

## API 문서
- 명세: [`../docs/03_API_SPEC.md`](../docs/03_API_SPEC.md)

## 주요 엔드포인트 (현재 스캐폴딩 상태)

| Method | Path | 상태 |
|--------|------|------|
| POST | /api/auth/signup | ✅ 동작 (JWT 미연동) |
| POST | /api/auth/login | ⚠️ JWT 토큰 미발급 (TODO) |
| GET  | /api/flights/search | ✅ 동작 (시드 데이터 기반) |
| GET  | /api/flights/{id} | ✅ 동작 |
| POST | /api/bookings | ✅ 동작 (인증 우회 — X-User-Id 헤더로 임시) |
| GET  | /api/bookings | ✅ 동작 |
| POST | /api/payments/confirm | ⚠️ TossPayments 시크릿 키 필요 |

## TODO (Phase 1 완성을 위해)
1. `JwtTokenProvider` 구현 → `AuthService.login()`에서 실제 토큰 발급
2. `JwtAuthenticationFilter` 구현 → `SecurityConfig`에 등록
3. `@AuthenticationPrincipal`로 컨트롤러에서 userId 주입 (현재 X-User-Id 헤더 임시)
4. PENDING 예약 10분 타임아웃 스케줄러 + 좌석 복구 로직
5. 예약 취소 API (Phase 2)

## 패키지 구조
[`../docs/05_FOLDER_STRUCTURE.md`](../docs/05_FOLDER_STRUCTURE.md) 참고.
