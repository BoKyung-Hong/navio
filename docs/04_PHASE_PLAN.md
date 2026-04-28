# Phase별 개발 계획

> 한 번에 다 만들지 말고 단계별로. 각 Phase가 끝나면 항상 "동작하는 서비스" 상태가 되도록 설계됨.

---

## Phase 1 — MVP (최우선) 🎯

**목표**: "검색 → 예약 → 결제 → 내 예약 확인" 핵심 흐름이 동작한다.

### 1-A. 백엔드 기반

- [ ] Spring Boot 3.x 프로젝트 부트스트랩 (build.gradle, application.yml)
- [ ] MySQL + Redis 연결 (docker-compose 활용)
- [ ] JPA 엔티티 8개 작성 (`02_ERD.md` 참고)
- [ ] data.sql로 시드 데이터 주입 (공항 6개 + 항공편 21건)
- [ ] 글로벌 예외 핸들러 (`@RestControllerAdvice`) + 공통 응답 포맷

### 1-B. 인증

- [ ] Spring Security FilterChain 설정 (Stateless)
- [ ] JWT 발급/검증 유틸 (`JwtTokenProvider`)
- [ ] `JwtAuthenticationFilter` — 모든 요청 인증
- [ ] `POST /api/auth/signup`, `POST /api/auth/login`, `POST /api/auth/refresh`
- [ ] 비밀번호 BCrypt 해싱
- [ ] Refresh Token Redis 저장

### 1-C. 항공편 검색

- [ ] `Flight`, `Airport`, `SeatInventory` 엔티티 + 레포지토리
- [ ] `FlightSearchService` — 출발지/도착지/날짜 필터링
- [ ] `GET /api/flights/search`, `GET /api/flights/{id}`
- [ ] DTO 매핑 (`FlightResponse`, `SeatResponse`)

### 1-D. 예약 + 좌석 재고 (핵심 ⭐)

- [ ] `Booking`, `Passenger` 엔티티
- [ ] `BookingService.createBooking()` — 비관적 락으로 좌석 차감
- [ ] 예약번호 생성 로직 (`NV{YYYYMMDD}{4자리랜덤}`)
- [ ] `POST /api/bookings`, `GET /api/bookings`, `GET /api/bookings/{number}`
- [ ] PENDING 상태 + 결제 마감 10분 타임아웃 (스케줄러)

### 1-E. 결제 (TossPayments)

- [ ] `Payment` 엔티티
- [ ] TossPayments 시크릿 키 환경변수 분리
- [ ] `POST /api/payments/confirm` — TossPayments 서버 검증 + 금액 일치 확인
- [ ] 결제 성공 시 `Booking.status` → CONFIRMED
- [ ] 결제 실패 시 좌석 복구 + Booking CANCELLED

### 1-F. 프론트엔드

- [ ] Vite + React + TS + Tailwind 부트스트랩
- [ ] axios 인스턴스 (인터셉터로 JWT 자동 주입 + 만료 시 refresh)
- [ ] 페이지: `Login`, `Signup`, `Search`, `FlightDetail`, `BookingForm`, `Payment`, `MyBookings`, `BookingDetail`
- [ ] 라우팅: React Router
- [ ] 전역 상태: Zustand 또는 Context (사용자/토큰)
- [ ] TossPayments 위젯 연동 (`@tosspayments/payment-widget-sdk`)

### Phase 1 완료 기준 (Definition of Done)
- 회원가입 → 로그인 → 검색 → 예약 → 결제 → 마이페이지 흐름이 끊김 없이 동작
- 동시 예약 테스트(같은 좌석에 2개 요청 동시 전송) 시 1건만 성공
- 결제 실패 시 좌석이 자동 복구됨

---

## Phase 2 — 차별화 기능

### 2-A. 알람 스케줄러
- [ ] `Notification` 엔티티 + 발송 큐
- [ ] 예약 완료 시 D-7, D-3, D-1, 체크인 오픈, 체크인 마감 2시간 전, 탑승 시작 알림 예약
- [ ] `JavaMailSender` + `@Scheduled` 1분마다 발송 대상 조회
- [ ] 이메일 템플릿 (Thymeleaf)

### 2-B. 예약 취소 / 환불
- [ ] `POST /api/bookings/{number}/cancel`
- [ ] TossPayments 부분/전액 환불 API 호출
- [ ] 좌석 자동 복구
- [ ] 환불 상태 추적 (REFUND_REQUESTED → REFUNDED)

### 2-C. 수하물·체크인 정보 강화
- [ ] 항공편 상세 화면에 캐리어 사이즈/무게 시각화
- [ ] 현지시간 vs 한국시간 토글

---

## Phase 3 — AI 도우미

- [ ] Anthropic Claude API 연동 (백엔드)
- [ ] `POST /api/chat` — 사용자 질문 + 예약 컨텍스트 → System Prompt 주입 → Claude 호출
- [ ] 프론트 챗 UI (플로팅 버튼)
- [ ] 자주 묻는 질문 정적 응답 fallback

---

## Phase 4 — 운영 / 확장

- [ ] 카카오 OAuth2 소셜 로그인
- [ ] 좌석 선택 UI (좌석 맵)
- [ ] 마일리지 / 포인트
- [ ] 관리자 대시보드 (예약 현황, 매출)
- [ ] AWS EC2 배포 + Nginx 리버스 프록시
- [ ] CI/CD (GitHub Actions)
- [ ] 모니터링 (Prometheus + Grafana)

---

## 권장 작업 순서 (1주차 ~ 4주차 가이드)

| 주차 | 백엔드 | 프론트엔드 |
|------|--------|----------|
| 1주차 | 1-A 기반 + 1-B 인증 | 부트스트랩 + Login/Signup |
| 2주차 | 1-C 검색 + 1-D 예약 | Search + FlightDetail + BookingForm |
| 3주차 | 1-E 결제 | Payment + MyBookings |
| 4주차 | 통합 테스트 + 버그 수정 | UI 다듬기 + 반응형 |

> 혼자 하는 프로젝트라면 8~10주가 현실적이에요. 무리하지 말고 한 단계씩.
