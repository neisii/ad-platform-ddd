# Quick Start Guide

## 5분 안에 Campaign Service 실행하기

### 1단계: 프로젝트 클론 (이미 완료)
```bash
cd /Users/neisii/Development/ad-platform-ddd
```

### 2단계: Docker로 서비스 기동
```bash
# PostgreSQL + Campaign Service 기동
docker-compose up -d

# 로그 확인 (서비스가 준비될 때까지 대기)
docker-compose logs -f campaign-service
# "Started CampaignServiceApplication" 메시지 확인 후 Ctrl+C
```

### 3단계: API 테스트

#### 3-1. 캠페인 생성
```bash
curl -X POST http://localhost:8082/api/v1/campaigns \
  -H "Content-Type: application/json" \
  -d '{
    "advertiserId": "adv-1",
    "name": "Summer Sale 2026",
    "dailyBudget": 100000,
    "totalBudget": 3000000,
    "startDate": "2026-06-01",
    "endDate": "2026-08-31"
  }' | jq
```

**예상 응답:**
```json
{
  "id": "camp-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "advertiserId": "adv-1",
  "name": "Summer Sale 2026",
  "status": "ACTIVE",
  "budget": {
    "dailyBudget": 100000,
    "totalBudget": 3000000,
    "spent": 0,
    "remainingDaily": 100000,
    "remainingTotal": 3000000
  },
  "startDate": "2026-06-01",
  "endDate": "2026-08-31"
}
```

📝 **응답의 `id` 값을 복사하세요!** (다음 단계에서 사용)

#### 3-2. 캠페인 조회
```bash
# {campaignId}를 위에서 복사한 ID로 교체
curl http://localhost:8082/api/v1/campaigns/{campaignId} | jq
```

#### 3-3. 광고그룹 추가
```bash
# {campaignId}를 위에서 복사한 ID로 교체
curl -X POST http://localhost:8082/api/v1/campaigns/{campaignId}/ad-groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product Category A",
    "bid": 500
  }' | jq
```

**예상 응답:**
```json
{
  "id": "ag-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "campaignId": "camp-...",
  "name": "Product Category A",
  "bid": 500,
  "status": "ACTIVE"
}
```

#### 3-4. 캠페인 상태 변경
```bash
# {campaignId}를 위에서 복사한 ID로 교체
curl -X PATCH http://localhost:8082/api/v1/campaigns/{campaignId}/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PAUSED"
  }' | jq
```

#### 3-5. 광고주별 캠페인 목록 조회
```bash
curl "http://localhost:8082/api/v1/campaigns?advertiserId=adv-1" | jq
```

### 4단계: 서비스 중지
```bash
# 서비스 중지 (데이터 유지)
docker-compose down

# 서비스 중지 + 데이터 삭제
docker-compose down -v
```

---

## 로컬 개발 환경 (선택사항)

### 사전 요구사항
- Java 17
- Gradle 8.5+

### 실행 방법

#### 1. PostgreSQL만 Docker로 실행
```bash
docker-compose up -d postgres
```

#### 2. Campaign Service를 로컬에서 실행
```bash
./gradlew :campaign-module:bootRun
```

#### 3. 테스트 실행
```bash
# 전체 테스트
./gradlew :campaign-module:test

# 특정 테스트 클래스
./gradlew :campaign-module:test --tests CampaignTest

# 테스트 결과 확인
open campaign-module/build/reports/tests/test/index.html
```

#### 4. 빌드
```bash
./gradlew :campaign-module:build
```

---

## 문제 해결

### 포트 충돌
```bash
# 5432 포트가 이미 사용 중인 경우
# docker-compose.yml에서 포트 변경:
# ports:
#   - "5433:5432"
```

### Docker 로그 확인
```bash
# PostgreSQL
docker-compose logs postgres

# Campaign Service
docker-compose logs campaign-service

# 실시간 로그
docker-compose logs -f campaign-service
```

### 데이터베이스 초기화
```bash
docker-compose down -v
docker-compose up -d
```

### 서비스 Health Check
```bash
# Health check endpoint (Actuator 사용)
curl http://localhost:8082/actuator/health
```

---

## 다음 단계

1. **ARCHITECTURE.md** 읽기 - 전체 아키텍처 이해
2. **IMPLEMENTATION_SUMMARY.md** 읽기 - 구현 세부사항
3. **Domain 테스트 코드** 살펴보기 - TDD 예시
4. **다른 모듈 구현** 시도 (Advertiser, Targeting 등)

---

## 추가 리소스

- API 문서: README.md의 "API 사용 예시" 섹션
- 도메인 모델: ARCHITECTURE.md의 "Campaign Module 상세 설계" 섹션
- 테스트 전략: IMPLEMENTATION_SUMMARY.md의 "테스트 커버리지 요약" 섹션
