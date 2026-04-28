# Navio 데이터베이스 설계 (ERD)

> MVP(Phase 1)에 필요한 최소 테이블 + Phase 2 확장 여지를 남긴 설계.
> RDB는 MySQL 8, 캐시·락은 Redis.

---

## 1. 전체 테이블 목록

| # | 테이블 | 한 줄 설명 | Phase |
|---|--------|-----------|-------|
| 1 | `users` | 회원 정보 | 1 |
| 2 | `airports` | 공항 마스터 (ICN, NRT 등) | 1 |
| 3 | `flights` | 항공편 스케줄 | 1 |
| 4 | `seat_inventory` | 항공편별 좌석 재고 (Economy/Business) | 1 |
| 5 | `bookings` | 예약 (예약번호, 사용자, 항공편) | 1 |
| 6 | `passengers` | 예약별 탑승객 정보 (1:N) | 1 |
| 7 | `payments` | 결제 내역 (TossPayments 연동) | 1 |
| 8 | `refresh_tokens` | JWT Refresh Token (또는 Redis로 대체) | 1 |
| 9 | `notifications` | 알람 발송 예약/이력 | 2 |
| 10 | `chat_messages` | AI 도우미 대화 로그 | 2 |

---

## 2. 테이블 정의

### users

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 사용자 ID |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 로그인 ID |
| password | VARCHAR(255) | NOT NULL | BCrypt 해시 |
| name | VARCHAR(50) | NOT NULL | 이름 |
| phone | VARCHAR(20) | NULL | 연락처 |
| role | VARCHAR(20) | DEFAULT 'USER' | USER / ADMIN |
| created_at | DATETIME | NOT NULL | 생성일시 |
| updated_at | DATETIME | NOT NULL | 수정일시 |

### airports

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| code | VARCHAR(3) | PK | IATA 코드 (ICN, NRT, LAX) |
| name_ko | VARCHAR(50) | NOT NULL | 한글 이름 |
| name_en | VARCHAR(100) | NOT NULL | 영문 이름 |
| city | VARCHAR(50) | NOT NULL | 도시 |
| country | VARCHAR(50) | NOT NULL | 국가 |
| timezone | VARCHAR(50) | NOT NULL | Asia/Seoul, Asia/Tokyo 등 |

### flights

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 항공편 ID |
| flight_number | VARCHAR(10) | NOT NULL | OZ102 |
| airline | VARCHAR(50) | NOT NULL | 아시아나항공 |
| departure_airport | VARCHAR(3) | FK → airports.code | 출발 공항 |
| arrival_airport | VARCHAR(3) | FK → airports.code | 도착 공항 |
| departure_time | DATETIME | NOT NULL | 출발 시각 (현지 시각) |
| arrival_time | DATETIME | NOT NULL | 도착 시각 (현지 시각) |
| aircraft_type | VARCHAR(20) | NULL | A380, B777 |
| checkin_open_minutes | INT | DEFAULT 1440 | 출발 N분 전 체크인 오픈 (24시간 = 1440) |
| checkin_close_minutes | INT | DEFAULT 60 | 출발 N분 전 체크인 마감 |
| created_at | DATETIME | NOT NULL | |

**인덱스**: `(departure_airport, arrival_airport, departure_time)` — 검색 성능

### seat_inventory

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| flight_id | BIGINT | FK → flights.id | |
| seat_class | VARCHAR(20) | NOT NULL | ECONOMY / BUSINESS / FIRST |
| total_seats | INT | NOT NULL | 총 좌석 수 |
| available_seats | INT | NOT NULL | 남은 좌석 수 |
| price | INT | NOT NULL | 가격 (원) |
| version | BIGINT | DEFAULT 0 | 낙관적 락 버전 (보조) |

**유니크 제약**: `(flight_id, seat_class)`

> ⚠️ 동시 예약 시 비관적 락(`SELECT ... FOR UPDATE`) 적용. Redis 분산 락도 옵션.

### bookings

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| booking_number | VARCHAR(20) | UNIQUE, NOT NULL | 예약번호 (NV20260427XXXX) |
| user_id | BIGINT | FK → users.id | |
| flight_id | BIGINT | FK → flights.id | |
| seat_class | VARCHAR(20) | NOT NULL | |
| passenger_count | INT | NOT NULL | 탑승객 수 |
| total_price | INT | NOT NULL | 총 결제액 |
| status | VARCHAR(20) | NOT NULL | PENDING / CONFIRMED / CANCELLED / REFUNDED |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**상태 전이**:
```
PENDING ──결제 성공──> CONFIRMED ──취소 요청──> CANCELLED ──환불──> REFUNDED
   │
   └─결제 실패/타임아웃─> CANCELLED
```

### passengers

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| booking_id | BIGINT | FK → bookings.id | |
| name_korean | VARCHAR(50) | NULL | 한글 이름 |
| name_english | VARCHAR(100) | NOT NULL | 영문 이름 (여권 표기) |
| birth_date | DATE | NOT NULL | 생년월일 |
| gender | VARCHAR(10) | NOT NULL | MALE / FEMALE |
| passport_number | VARCHAR(20) | NULL | 여권번호 (Phase 2) |
| nationality | VARCHAR(3) | NULL | KOR, USA |

### payments

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| booking_id | BIGINT | FK → bookings.id, UNIQUE | 예약 1건 = 결제 1건 |
| order_id | VARCHAR(64) | UNIQUE, NOT NULL | TossPayments orderId |
| payment_key | VARCHAR(200) | NULL | TossPayments paymentKey |
| method | VARCHAR(20) | NULL | CARD / TRANSFER / VIRTUAL_ACCOUNT |
| amount | INT | NOT NULL | 결제 금액 |
| status | VARCHAR(20) | NOT NULL | READY / IN_PROGRESS / DONE / CANCELED / FAILED |
| failure_reason | VARCHAR(255) | NULL | 실패 사유 |
| paid_at | DATETIME | NULL | 결제 완료 시각 |
| created_at | DATETIME | NOT NULL | |

### refresh_tokens (또는 Redis로 대체)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK | |
| user_id | BIGINT | FK → users.id | |
| token | VARCHAR(500) | UNIQUE | |
| expires_at | DATETIME | NOT NULL | |

> 권장: Redis에 `refresh:userId={id}` → token 형태로 저장하면 강제 로그아웃이 쉽다.

---

## 3. 관계 다이어그램 (텍스트)

```
users (1) ──< (N) bookings (1) ──< (N) passengers
                       │
                       └── (1:1) payments

airports (1) ──< (N) flights (1) ──< (N) seat_inventory
                       │
                       └──< (N) bookings
```

## 4. 시드 데이터 전략

`backend/src/main/resources/data.sql` 또는 `DataInitializer.java`로 앱 시작 시 자동 주입.

**최소 시드 데이터**:
- 공항: ICN, NRT, KIX, LAX, JFK, CDG (6개)
- 항공편: ICN→NRT, ICN→LAX, ICN→CDG 각 노선 일주일치 (~21건)
- 좌석 재고: 항공편당 Economy 180 / Business 24
- 테스트 사용자: `test@navio.com` / `test1234`

## 5. 동시성 처리 핵심 흐름

```
[예약 요청]
    ↓
1. SELECT seat_inventory WHERE flight_id=? AND seat_class=? FOR UPDATE  ← 비관적 락
    ↓
2. available_seats >= passenger_count 확인
    ↓
3. UPDATE seat_inventory SET available_seats = available_seats - N
    ↓
4. INSERT INTO bookings (status='PENDING')
    ↓
5. 트랜잭션 커밋 → 락 해제
    ↓
6. 클라이언트에 booking_number 반환
    ↓
7. (별도 트랜잭션) 결제 진행 → 결제 성공 시 bookings.status = 'CONFIRMED'
```

> 결제 실패/타임아웃 시 → 좌석 복구 + bookings.status = 'CANCELLED'
