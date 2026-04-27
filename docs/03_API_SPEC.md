# Navio REST API 명세

> Phase 1 MVP API. 모든 응답은 JSON. 인증이 필요한 엔드포인트는 `Authorization: Bearer {accessToken}` 헤더 필수.

---

## 공통 규칙

### Base URL
- 로컬: `http://localhost:8080/api`

### 응답 포맷

**성공**
```json
{
  "success": true,
  "data": { ... }
}
```

**실패**
```json
{
  "success": false,
  "error": {
    "code": "FLIGHT_NOT_FOUND",
    "message": "해당 항공편을 찾을 수 없습니다."
  }
}
```

### 표준 HTTP 상태 코드
- `200 OK` — 성공
- `201 Created` — 리소스 생성
- `400 Bad Request` — 입력값 오류
- `401 Unauthorized` — 인증 실패
- `403 Forbidden` — 권한 없음
- `404 Not Found` — 리소스 없음
- `409 Conflict` — 좌석 부족, 중복 예약 등
- `500 Internal Server Error` — 서버 오류

---

## 1. 인증 (Auth)

### POST /api/auth/signup
회원가입

```json
// Request
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "phone": "010-1234-5678"
}

// Response 201
{
  "success": true,
  "data": { "userId": 1, "email": "user@example.com" }
}
```

### POST /api/auth/login
로그인

```json
// Request
{ "email": "user@example.com", "password": "password123" }

// Response 200
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "user": { "id": 1, "email": "user@example.com", "name": "홍길동" }
  }
}
```

### POST /api/auth/refresh
Access Token 재발급

```json
// Request
{ "refreshToken": "eyJhbGc..." }

// Response 200
{ "success": true, "data": { "accessToken": "eyJhbGc..." } }
```

### POST /api/auth/logout
로그아웃 (Refresh Token 폐기) — 인증 필요

---

## 2. 항공편 (Flights)

### GET /api/flights/search
항공편 검색

**Query Params**
- `departure` (필수): 출발 공항 코드 (ICN)
- `arrival` (필수): 도착 공항 코드 (NRT)
- `date` (필수): 출발 날짜 (2026-05-01)
- `seatClass` (선택): ECONOMY / BUSINESS / FIRST
- `passengers` (선택, 기본 1): 탑승객 수

```json
// Response 200
{
  "success": true,
  "data": {
    "flights": [
      {
        "id": 1,
        "flightNumber": "OZ102",
        "airline": "아시아나항공",
        "departure": {
          "airport": "ICN",
          "airportName": "인천국제공항",
          "time": "2026-05-01T09:00:00",
          "timezone": "Asia/Seoul"
        },
        "arrival": {
          "airport": "NRT",
          "airportName": "나리타국제공항",
          "time": "2026-05-01T11:30:00",
          "timezone": "Asia/Tokyo"
        },
        "duration": "2h 30m",
        "aircraftType": "A380",
        "seats": [
          { "class": "ECONOMY", "available": 42, "price": 350000 },
          { "class": "BUSINESS", "available": 5, "price": 1200000 }
        ],
        "checkin": {
          "openMinutesBefore": 1440,
          "closeMinutesBefore": 60
        }
      }
    ]
  }
}
```

### GET /api/flights/{flightId}
항공편 상세

```json
// Response 200
{
  "success": true,
  "data": {
    "id": 1,
    "flightNumber": "OZ102",
    "airline": "아시아나항공",
    "departure": { ... },
    "arrival": { ... },
    "seats": [...],
    "baggage": {
      "carryOn": { "size": "55x40x20cm", "weight": "10kg" },
      "checked": { "weight": "23kg", "count": 1 }
    }
  }
}
```

---

## 3. 예약 (Bookings) — 인증 필요

### POST /api/bookings
예약 생성 (좌석 차감)

```json
// Request
{
  "flightId": 1,
  "seatClass": "ECONOMY",
  "passengers": [
    {
      "nameKorean": "홍길동",
      "nameEnglish": "HONG GILDONG",
      "birthDate": "1995-03-15",
      "gender": "MALE"
    }
  ]
}

// Response 201
{
  "success": true,
  "data": {
    "bookingId": 100,
    "bookingNumber": "NV20260427A1B2",
    "status": "PENDING",
    "totalPrice": 350000,
    "expiresAt": "2026-04-27T15:30:00"  // 결제 마감 (10분)
  }
}

// Response 409 (좌석 부족)
{
  "success": false,
  "error": { "code": "SOLD_OUT", "message": "좌석이 부족합니다." }
}
```

### GET /api/bookings
내 예약 목록

```json
// Response 200
{
  "success": true,
  "data": {
    "bookings": [
      {
        "bookingNumber": "NV20260427A1B2",
        "status": "CONFIRMED",
        "flight": { "flightNumber": "OZ102", "departure": "ICN", "arrival": "NRT", "departureTime": "2026-05-01T09:00:00" },
        "passengerCount": 1,
        "totalPrice": 350000,
        "createdAt": "2026-04-27T15:20:00"
      }
    ]
  }
}
```

### GET /api/bookings/{bookingNumber}
예약 상세

### POST /api/bookings/{bookingNumber}/cancel
예약 취소

---

## 4. 결제 (Payments) — 인증 필요

### POST /api/payments/confirm
TossPayments 결제 승인 (프론트엔드에서 위젯 결제 완료 후 호출)

```json
// Request
{
  "paymentKey": "tgen_20240114195918n23Aw",
  "orderId": "NV20260427A1B2",
  "amount": 350000
}

// Response 200
{
  "success": true,
  "data": {
    "paymentId": 1,
    "status": "DONE",
    "method": "CARD",
    "paidAt": "2026-04-27T15:25:00",
    "booking": { "bookingNumber": "NV20260427A1B2", "status": "CONFIRMED" }
  }
}

// Response 400 (금액 불일치)
{
  "success": false,
  "error": { "code": "AMOUNT_MISMATCH", "message": "결제 금액이 일치하지 않습니다." }
}
```

### GET /api/payments/{orderId}
결제 상태 조회

---

## 5. 사용자 (Me) — 인증 필요

### GET /api/users/me
내 정보 조회

### PATCH /api/users/me
내 정보 수정 (이름, 전화번호)

---

## 에러 코드 목록

| 코드 | 설명 |
|------|------|
| `INVALID_CREDENTIALS` | 이메일/비밀번호 불일치 |
| `EMAIL_ALREADY_EXISTS` | 이메일 중복 |
| `TOKEN_EXPIRED` | 토큰 만료 |
| `TOKEN_INVALID` | 토큰 위조/손상 |
| `FLIGHT_NOT_FOUND` | 항공편 없음 |
| `SOLD_OUT` | 좌석 부족 |
| `BOOKING_NOT_FOUND` | 예약 없음 |
| `BOOKING_EXPIRED` | 결제 마감 시간 초과 |
| `AMOUNT_MISMATCH` | 결제 금액 불일치 |
| `PAYMENT_FAILED` | 결제 실패 |
| `ALREADY_CANCELLED` | 이미 취소된 예약 |
