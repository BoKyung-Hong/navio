# 🚀 START HERE — 처음 실행하는 가이드

> Java 17, Node 18+, Docker Desktop이 모두 설치돼 있다면 아래 한 줄로 끝.

## 자동 설정 (권장)

PowerShell을 열고:

```powershell
cd C:\Navio\NAVIO
./setup.ps1
```

이 스크립트가 다음을 자동으로 수행합니다:
1. Java/Node/Docker 설치 확인
2. Docker로 MySQL + Redis 실행
3. Gradle Wrapper JAR 다운로드
4. 프론트엔드 `npm install`
5. 백엔드 첫 빌드 (의존성 다운로드)

PowerShell 실행 정책 오류가 나면 한 번만:
```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

---

## 수동 설정 (스크립트가 싫으면)

### 1단계 — Docker로 DB 띄우기
```powershell
cd C:\Navio\NAVIO
docker compose up -d
```

### 2단계 — Gradle Wrapper JAR 다운로드 (한 번만)
```powershell
Invoke-WebRequest `
  -Uri "https://services.gradle.org/distributions/gradle-8.7-wrapper.jar" `
  -OutFile "backend\gradle\wrapper\gradle-wrapper.jar"
```

> **또는 IntelliJ로 더 쉽게**: IntelliJ에서 `File → Open` → `backend` 폴더 선택 → "Open as Gradle Project"
> → IntelliJ가 wrapper jar를 자동으로 만들어줍니다.

### 3단계 — 프론트엔드 의존성 설치
```powershell
cd C:\Navio\NAVIO\frontend

# (이전에 실패한 설치가 있다면 정리)
Remove-Item -Recurse -Force node_modules -ErrorAction SilentlyContinue

# .env.local 만들기
Copy-Item .env.example .env.local

npm install
```

### 4단계 — 실행
```powershell
# 터미널 1
cd C:\Navio\NAVIO\backend
./gradlew.bat bootRun

# 터미널 2
cd C:\Navio\NAVIO\frontend
npm run dev
```

브라우저에서:
- 프론트: http://localhost:5173
- 백엔드 헬스체크: http://localhost:8080/actuator/health

---

## 서비스 종료

```powershell
# 백엔드/프론트는 터미널에서 Ctrl+C
# DB 컨테이너 끄기
cd C:\Navio\NAVIO
docker compose down
```

데이터까지 완전 초기화하려면:
```powershell
docker compose down -v
```

---

## 문제 해결

### Q. `./gradlew.bat`이 안 돼요
- `gradle-wrapper.jar`가 없을 가능성 99%. 위의 2단계 다시 실행하거나 IntelliJ로 열어주세요.

### Q. PowerShell에서 `./setup.ps1`이 막혀요
- 실행 정책 변경: `Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned`

### Q. `docker compose up`이 안 돼요
- Docker Desktop이 실행 중인지 확인 (트레이에 고래 🐳 아이콘)
- 3306/6379 포트가 이미 사용 중인지: `netstat -ano | findstr 3306`

### Q. 브라우저에서 검색해도 결과가 안 나와요
- 백엔드 콘솔에 `[DataInitializer] Done. flights=...` 로그가 떴는지 확인
- 안 떴으면 DB 연결 실패. `application-local.yml`의 DB 정보 확인

### Q. node_modules 설치하다 에러가 나요
- 부분 설치된 상태가 남아있을 수 있어요. 폴더 통째로 삭제 후 재시도:
  ```powershell
  cd C:\Navio\NAVIO\frontend
  Remove-Item -Recurse -Force node_modules
  npm install
  ```

---

## 다음에 할 일

설정이 끝났으면:

1. [docs/01_OVERVIEW.md](docs/01_OVERVIEW.md) — 프로젝트가 뭔지 다시 정리
2. [docs/04_PHASE_PLAN.md](docs/04_PHASE_PLAN.md) — Phase 1 체크리스트로 개발 시작
3. [backend/README.md](backend/README.md) / [frontend/README.md](frontend/README.md) — 각 모듈의 TODO 확인
