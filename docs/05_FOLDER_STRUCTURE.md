# 프로젝트 폴더 구조

## 루트

```
NAVIO/
├── docs/                  ← 설계 문서
├── backend/               ← Spring Boot
├── frontend/              ← React + TS
├── docker-compose.yml     ← MySQL + Redis 로컬 실행
├── .gitignore
└── README.md
```

---

## backend/ (Spring Boot)

```
backend/
├── build.gradle           ← 의존성 정의
├── settings.gradle
├── gradlew, gradlew.bat   ← Gradle Wrapper (이후 생성)
├── src/
│   ├── main/
│   │   ├── java/com/navio/
│   │   │   ├── NavioApplication.java       ← 메인 클래스
│   │   │   │
│   │   │   ├── config/                     ← 설정
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── DataInitializer.java    ← 시드 데이터 주입
│   │   │   │
│   │   │   ├── common/                     ← 공통 유틸
│   │   │   │   ├── ApiResponse.java        ← 공통 응답 포맷
│   │   │   │   ├── ErrorCode.java          ← 에러 코드 enum
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   └── domain/
│   │   │       ├── user/
│   │   │       │   ├── User.java
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   └── dto/
│   │   │       │       ├── SignupRequest.java
│   │   │       │       ├── LoginRequest.java
│   │   │       │       └── TokenResponse.java
│   │   │       │
│   │   │       ├── flight/
│   │   │       │   ├── Flight.java
│   │   │       │   ├── Airport.java
│   │   │       │   ├── FlightRepository.java
│   │   │       │   ├── AirportRepository.java
│   │   │       │   ├── FlightSearchService.java
│   │   │       │   ├── FlightController.java
│   │   │       │   └── dto/
│   │   │       │       └── FlightResponse.java
│   │   │       │
│   │   │       ├── seat/
│   │   │       │   ├── SeatInventory.java
│   │   │       │   ├── SeatClass.java       ← enum
│   │   │       │   ├── SeatInventoryRepository.java
│   │   │       │   └── SeatService.java     ← 비관적 락 차감
│   │   │       │
│   │   │       ├── booking/
│   │   │       │   ├── Booking.java
│   │   │       │   ├── BookingStatus.java   ← enum
│   │   │       │   ├── Passenger.java
│   │   │       │   ├── BookingRepository.java
│   │   │       │   ├── BookingService.java
│   │   │       │   ├── BookingController.java
│   │   │       │   └── dto/
│   │   │       │       ├── CreateBookingRequest.java
│   │   │       │       └── BookingResponse.java
│   │   │       │
│   │   │       └── payment/
│   │   │           ├── Payment.java
│   │   │           ├── PaymentStatus.java   ← enum
│   │   │           ├── PaymentRepository.java
│   │   │           ├── PaymentService.java
│   │   │           ├── PaymentController.java
│   │   │           ├── TossPaymentsClient.java   ← Toss API 호출
│   │   │           └── dto/
│   │   │               └── ConfirmPaymentRequest.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── data.sql            ← 시드 데이터 (선택)
│   │       └── static/
│   │
│   └── test/
│       └── java/com/navio/
│           └── (테스트 코드)
└── README.md
```

### 패키지 구조 원칙
- **도메인별 패키지**(domain/user, domain/flight) — 기능 응집도 높음
- 각 도메인 안에 Entity, Repository, Service, Controller, DTO 모두 위치
- 다른 도메인을 참조할 때는 Service 단에서만 (Repository 직접 참조 금지)

---

## frontend/ (React + TS + Vite)

```
frontend/
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.js
├── postcss.config.js
├── index.html
├── public/
│   └── favicon.svg
└── src/
    ├── main.tsx                 ← 엔트리
    ├── App.tsx                  ← 라우터
    ├── index.css                ← Tailwind import
    │
    ├── pages/
    │   ├── HomePage.tsx
    │   ├── LoginPage.tsx
    │   ├── SignupPage.tsx
    │   ├── SearchPage.tsx
    │   ├── FlightDetailPage.tsx
    │   ├── BookingFormPage.tsx
    │   ├── PaymentPage.tsx
    │   ├── MyBookingsPage.tsx
    │   └── BookingDetailPage.tsx
    │
    ├── components/
    │   ├── layout/
    │   │   ├── Header.tsx
    │   │   └── Footer.tsx
    │   ├── flight/
    │   │   ├── FlightCard.tsx
    │   │   └── SearchForm.tsx
    │   ├── booking/
    │   │   └── PassengerInput.tsx
    │   └── ui/
    │       ├── Button.tsx
    │       ├── Input.tsx
    │       └── Modal.tsx
    │
    ├── api/                     ← 백엔드 API 호출
    │   ├── client.ts            ← axios 인스턴스
    │   ├── auth.ts
    │   ├── flights.ts
    │   ├── bookings.ts
    │   └── payments.ts
    │
    ├── hooks/
    │   ├── useAuth.ts
    │   └── useFlightSearch.ts
    │
    ├── types/                   ← 공통 타입
    │   ├── flight.ts
    │   ├── booking.ts
    │   └── user.ts
    │
    ├── lib/
    │   └── tossPayments.ts      ← TossPayments 위젯 래퍼
    │
    └── styles/
        └── (필요 시 글로벌 스타일)
```

### 컴포넌트 작성 원칙
- 페이지는 `pages/`, 재사용 컴포넌트는 `components/`
- API 호출은 절대 컴포넌트에서 직접 fetch하지 말고 `api/` 모듈 통해서
- 타입은 `types/`에 모아두고 공유

---

## 환경 변수 분리

### backend/.env (또는 application-local.yml)
```
SPRING_PROFILES_ACTIVE=local
DB_URL=jdbc:mysql://localhost:3306/navio
DB_USERNAME=navio
DB_PASSWORD=navio_pw
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=change-me-32-chars-or-longer-secret
JWT_ACCESS_EXPIRY=1800000        # 30분
JWT_REFRESH_EXPIRY=604800000     # 7일
TOSS_SECRET_KEY=test_sk_xxx
TOSS_CLIENT_KEY=test_ck_xxx
```

### frontend/.env.local
```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_TOSS_CLIENT_KEY=test_ck_xxx
```

> ⚠️ `.env*` 파일은 `.gitignore`에 반드시 포함.
