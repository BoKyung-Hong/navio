# ✈️ Navio

> Navigate(항법·항로를 탐색하다) + -io
> "항로를 탐색하듯, 승객이 예약 과정에서 길을 잃지 않도록 안내한다"

항공 예약 과정의 혼란(날짜·시간·체크인·수하물·결제·상태 추적·탑승 누락)을 해결하는 사용자 중심 항공 예약 서비스.

## 📚 시작하기 (3단계)

### 1. 문서 읽기
| 순서 | 문서 | 읽는 이유 |
|------|------|----------|
| 1 | [docs/01_OVERVIEW.md](docs/01_OVERVIEW.md) | 프로젝트가 뭔지, MVP가 뭔지 |
| 2 | [docs/06_DEV_ENVIRONMENT.md](docs/06_DEV_ENVIRONMENT.md) | 환경 세팅 가이드 |
| 3 | [docs/04_PHASE_PLAN.md](docs/04_PHASE_PLAN.md) | Phase 1부터 차근차근 |

### 2. 환경 띄우기
```bash
# MySQL + Redis 컨테이너 실행
docker compose up -d
```

### 3. 백엔드 + 프론트엔드 실행
```bash
# 터미널 1
cd backend && ./gradlew bootRun

# 터미널 2
cd frontend && npm install && npm run dev
```

- 백엔드: http://localhost:8080
- 프론트: http://localhost:5173

## 🗂 프로젝트 구조

```
NAVIO/
├── docs/              ← 설계 문서 (꼭 읽기!)
├── backend/           ← Spring Boot 3 + Java 17
├── frontend/          ← React 18 + TypeScript + Vite
├── docker-compose.yml ← 로컬 MySQL + Redis
└── README.md
```

## 📖 문서 인덱스

- [01_OVERVIEW.md](docs/01_OVERVIEW.md) — 프로젝트 개요 & MVP 정의
- [02_ERD.md](docs/02_ERD.md) — 데이터베이스 스키마
- [03_API_SPEC.md](docs/03_API_SPEC.md) — REST API 명세
- [04_PHASE_PLAN.md](docs/04_PHASE_PLAN.md) — Phase별 개발 계획 (체크리스트)
- [05_FOLDER_STRUCTURE.md](docs/05_FOLDER_STRUCTURE.md) — 폴더 구조 상세
- [06_DEV_ENVIRONMENT.md](docs/06_DEV_ENVIRONMENT.md) — 개발 환경 세팅 가이드

## 🔧 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Java 17, Spring Boot 3.2, JPA, Spring Security, Redis |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS, Zustand |
| Database | MySQL 8 |
| Cache | Redis 7 |
| Payment | TossPayments |
| Build | Gradle (BE), Vite (FE) |

## 🎯 Phase 1 MVP 범위

1. ✅ 회원가입 / 로그인 (JWT)
2. ✅ 항공편 검색 (Mock 시드 데이터)
3. ✅ 예약 + 좌석 재고 관리 (비관적 락)
4. ✅ TossPayments 결제 연동

> 알람, AI 도우미, 소셜 로그인 등은 Phase 2~4에서 추가됩니다.

## ⚠️ 현재 상태

이 저장소는 **스캐폴딩 단계**입니다. Phase 1 완성을 위해 [docs/04_PHASE_PLAN.md](docs/04_PHASE_PLAN.md)의 체크리스트를 따라가세요.

각 모듈의 README에 `TODO` 섹션이 있어요:
- [backend/README.md](backend/README.md)
- [frontend/README.md](frontend/README.md)
