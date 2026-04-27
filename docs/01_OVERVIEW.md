# Navio 프로젝트 개요

> ✈️ **Navio [네이비오]** — Navigate(항법·항로를 탐색하다) + -io(라틴어 어미)
> "항로를 탐색하듯, 승객이 예약 과정에서 길을 잃지 않도록 안내한다"

---

## 1. 프로젝트 한 줄 정의

항공 예약 과정에서 발생하는 **혼란(날짜·시간·체크인·수하물·결제·상태 추적·탑승 누락)**을 해결하는 사용자 중심 항공 예약 서비스.

## 2. MVP란?

**MVP = Minimum Viable Product = 최소 기능 제품**

- "이거 하나만 있어도 서비스라고 부를 수 있는 최소 기능"만 먼저 완성
- 나머지 기능은 Phase 2, Phase 3로 미루고 단계적으로 추가
- 한 번에 다 만들려고 하면 길을 잃기 때문에 단계를 나누는 것이 핵심

### Navio MVP 범위 (Phase 1)

다음 4가지만 우선 완성한다.

| # | 기능 | 설명 |
|---|------|------|
| 1 | 회원가입/로그인 | Spring Security + JWT 기반 이메일 로그인 |
| 2 | 항공편 검색 | Mock 시드 데이터 기반 출발지/도착지/날짜 검색 |
| 3 | 예약 + 좌석 재고 관리 | 비관적 락으로 동시성 제어, 좌석 차감 + 예약 생성 |
| 4 | TossPayments 결제 | 결제 위젯 + 서버 검증 + 예약 확정 |

> **Phase 2 이후로 미룬 것**: AI 도우미, 알람 스케줄러, 소셜 로그인, 마일리지, 좌석 선택 UI, 모바일 앱 등

## 3. 타겟 사용자

- **메인**: 2030 해외 자유 여행객 (항공권 직접 예약 경험 多, UX 편의성 중시)
- **서브**: 출장/업무 여행자 (일정 변경 빈번, 예약 상태 확인 중요)

## 4. 3가지 핵심 가치

1. **정보의 명확성** — 출발/도착 시간, 체크인 정보, 수하물 기준 명확 노출
2. **결제 신뢰성** — 결제 실패/중복 결제 문제 해결
3. **상태 추적의 투명성** — 예약·취소·환불 상태 실시간 확인

## 5. 기술 스택 한눈에 보기

| 영역 | 기술 | 이유 |
|------|------|------|
| Backend | Java 17 + Spring Boot 3.x | LTS 안정성, JPA/Security/Scheduler 내장 |
| Database | MySQL 8 | 트랜잭션 보장 (예약-결제-좌석 일관성) |
| Cache | Redis 7 | 좌석 분산 락, Refresh Token 저장 |
| Frontend | React 18 + TypeScript + Vite | 컴포넌트 기반, 타입 안정성 |
| Styling | Tailwind CSS | 빠른 UI 구현, 반응형 |
| Payment | TossPayments | 국내 표준, 위젯 방식 |
| Auth | Spring Security + JWT | Stateless, 모바일 확장 대비 |
| Build | Gradle (backend), Vite (frontend) | 표준 |

## 6. 디렉터리 구조

```
NAVIO/
├── docs/              ← 설계 문서 (지금 보는 곳)
├── backend/           ← Spring Boot 프로젝트
├── frontend/          ← React + TS 프로젝트
├── docker-compose.yml ← MySQL + Redis 로컬 실행용
├── .gitignore
└── README.md          ← 시작 가이드
```

각 폴더 안에 자체 README가 있다.

## 7. 다음에 할 일

1. `06_DEV_ENVIRONMENT.md`를 보고 로컬 환경 세팅 (Java 17, Node 18+, Docker)
2. `04_PHASE_PLAN.md`의 Phase 1 체크리스트 따라가기
3. `02_ERD.md`의 테이블부터 만들기
4. `03_API_SPEC.md`의 API 하나씩 구현
