# Navio Frontend

React 18 + TypeScript + Vite + Tailwind CSS.

## 실행

```bash
cd frontend
npm install
cp .env.example .env.local   # 그리고 값 채우기
npm run dev
```

브라우저: http://localhost:5173

## 폴더 구조

```
src/
├── pages/        ← 라우트별 페이지 컴포넌트
├── components/   ← 재사용 컴포넌트
├── api/          ← 백엔드 API 호출 (axios)
├── hooks/        ← 커스텀 훅 (useAuth 등)
├── types/        ← TypeScript 타입 정의
├── lib/          ← 외부 SDK 래퍼 (TossPayments 등)
└── styles/       ← 글로벌 스타일
```

## 라우트

| Path | Page |
|------|------|
| / | 홈 (검색 폼) |
| /login | 로그인 |
| /signup | 회원가입 |
| /search | 항공편 검색 결과 |
| /flights/:id | 항공편 상세 |
| /bookings/new | 예약 폼 (탑승객 입력) |
| /payment/:bookingNumber | 결제 |
| /my/bookings | 내 예약 목록 |
| /my/bookings/:bookingNumber | 예약 상세 |

## TODO (Phase 1 완성을 위해)
1. TossPayments 위젯 실제 통합 (`lib/tossPayments.ts`)
2. JWT refresh 인터셉터 (axios 401 시 자동 재발급)
3. 보호 라우트 (PrivateRoute) — 로그인 안 했으면 /login으로 리다이렉트
4. 페이지별 로딩/에러 UI 개선
5. 모바일 반응형 다듬기
