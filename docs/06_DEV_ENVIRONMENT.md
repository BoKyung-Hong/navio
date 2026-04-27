# 개발 환경 세팅 가이드

> 처음 환경을 세팅하는 사람을 기준으로 작성. 순서대로 따라가면 돼요.

---

## 1. 필수 설치 항목

| 항목 | 버전 | 다운로드 |
|------|------|----------|
| Java | 17 (LTS) | [Adoptium](https://adoptium.net/) |
| Node.js | 18 LTS 이상 | [Node.js](https://nodejs.org/) |
| Docker Desktop | 최신 | [Docker](https://www.docker.com/products/docker-desktop/) |
| Git | 최신 | [Git](https://git-scm.com/) |
| IntelliJ IDEA Community | 최신 | [JetBrains](https://www.jetbrains.com/idea/download/) |
| VS Code | 최신 | [VS Code](https://code.visualstudio.com/) |

### 설치 확인
```powershell
java -version           # openjdk 17.x.x
node -v                 # v18.x.x or v20.x.x
npm -v                  # 9.x.x or higher
docker --version        # Docker version 24.x or higher
git --version
```

---

## 2. Docker로 MySQL + Redis 띄우기

루트 폴더(`C:\Navio\NAVIO`)에서:

```powershell
docker compose up -d
```

이걸로 MySQL(3306)과 Redis(6379)가 백그라운드에서 실행됨. 끄려면:

```powershell
docker compose down
```

데이터를 완전히 초기화하고 싶으면:

```powershell
docker compose down -v
```

### 접속 확인
```powershell
# MySQL 접속
docker exec -it navio-mysql mysql -u navio -pnavio_pw navio

# Redis 접속
docker exec -it navio-redis redis-cli
```

---

## 3. 백엔드 실행

### IntelliJ에서
1. `backend/` 폴더를 IntelliJ로 열기 (Open as Gradle Project)
2. JDK 17 설정 (File → Project Structure → SDK)
3. Gradle 의존성 다운로드 대기
4. `NavioApplication.java` 우클릭 → Run

### 터미널에서
```powershell
cd backend
./gradlew bootRun
```

기본 포트: `http://localhost:8080`

### Health Check
```powershell
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

---

## 4. 프론트엔드 실행

```powershell
cd frontend
npm install
npm run dev
```

기본 포트: `http://localhost:5173`

브라우저에서 http://localhost:5173 접속하면 첫 화면이 보여야 정상.

---

## 5. TossPayments 테스트 키 발급

1. https://developers.tosspayments.com/ 회원가입
2. 내 개발 정보 → 테스트 키 복사
   - Client Key: `test_ck_...`
   - Secret Key: `test_sk_...`
3. 테스트 카드 번호로 결제 테스트 가능 (실제 결제 X)

### 환경 변수에 넣기
- `backend/src/main/resources/application-local.yml`에 `TOSS_SECRET_KEY` 입력
- `frontend/.env.local`에 `VITE_TOSS_CLIENT_KEY` 입력

---

## 6. 자주 만나는 문제

### Q1. `docker compose up`이 안 돼요
- Docker Desktop이 실행 중인지 확인
- 3306/6379 포트가 이미 사용 중이면 docker-compose.yml에서 포트 변경

### Q2. Spring Boot가 MySQL에 연결 못 해요
- `docker ps` 명령으로 컨테이너 실행 중인지 확인
- `application-local.yml`의 DB_URL/DB_USERNAME/DB_PASSWORD 확인

### Q3. CORS 에러
- 백엔드 `CorsConfig.java`에서 `http://localhost:5173` 허용 확인

### Q4. JWT가 만료됐다고 자꾸 뜨는데
- `JWT_SECRET`이 32자 이상인지 확인 (HS256 요구사항)
- 시스템 시간 동기화 (서버-클라이언트 시간 차이가 크면 토큰이 즉시 만료될 수 있음)

### Q5. `npm install` 중 에러
- `node -v`로 18 이상인지 확인
- `npm cache clean --force` 후 재시도

---

## 7. Git 시작하기

루트에서:

```powershell
cd C:\Navio\NAVIO
git init
git add .
git commit -m "chore: initial Navio scaffolding"
```

GitHub에 올리려면:
```powershell
git remote add origin https://github.com/{user}/navio.git
git branch -M main
git push -u origin main
```

---

## 8. 추천 IDE 플러그인

### IntelliJ
- Lombok
- Spring Boot Helper
- Database Tools

### VS Code
- ES7+ React/Redux/React-Native snippets
- Tailwind CSS IntelliSense
- Prettier
- ESLint

---

## 9. 다음 단계

환경 세팅이 끝나면 `04_PHASE_PLAN.md`의 **Phase 1-A**부터 시작.
첫 번째 목표는 "Spring Boot가 MySQL에 연결되고 헬스체크가 200 OK를 리턴" 하는 것.
