# 모듈 통합 테스트 가이드

## 구현 완료된 모듈

1. **Advertiser Service** (Port 8081) ✅
2. **Campaign Service** (Port 8082) ✅
3. **Targeting Service** (Port 8083) ✅
4. **Inventory Service** (Port 8084) ✅

## 전체 플로우 테스트

### 1단계: 모든 서비스 기동

```bash
# Docker Compose로 전체 시스템 기동
docker-compose up -d

# 서비스 상태 확인
docker-compose ps

# 로그 확인 (모든 서비스가 Started 로그 출력 확인)
docker-compose logs -f
```

### 2단계: 광고주 생성 (Advertiser Service)

```bash
curl -X POST http://localhost:8081/api/v1/advertisers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Company",
    "email": "test@example.com",
    "initialBalance": 1000000
  }' | jq

# 응답에서 "id" 값을 복사 (예: adv-xxxxx)
```

### 3단계: 캠페인 생성 (Campaign Service)

```bash
# {advertiserId}를 위에서 복사한 ID로 교체
curl -X POST http://localhost:8082/api/v1/campaigns \
  -H "Content-Type: application/json" \
  -d '{
    "advertiserId": "{advertiserId}",
    "name": "Summer Sale 2026",
    "dailyBudget": 100000,
    "totalBudget": 3000000,
    "startDate": "2026-01-23",
    "endDate": "2026-12-31"
  }' | jq

# 응답에서 "id" 값을 복사 (예: camp-xxxxx)
```

### 4단계: 광고그룹 추가 (Campaign Service)

```bash
# {campaignId}를 위에서 복사한 ID로 교체
curl -X POST http://localhost:8082/api/v1/campaigns/{campaignId}/ad-groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product Category A",
    "bid": 500
  }' | jq

# 응답에서 "id" 값을 복사 (예: ag-xxxxx)
```

### 5단계: 타겟팅 규칙 생성 (Targeting Service)

```bash
# {campaignId}를 캠페인 ID로 교체
curl -X POST http://localhost:8083/api/v1/targeting/rules \
  -H "Content-Type: application/json" \
  -d '{
    "campaignId": "{campaignId}",
    "ageMin": 20,
    "ageMax": 40,
    "gender": "M",
    "geoTargets": ["KR", "Seoul"],
    "deviceTypes": ["MOBILE"],
    "keywords": ["tech", "gaming"]
  }' | jq
```

### 6단계: 광고 지면 생성 (Inventory Service)

```bash
curl -X POST http://localhost:8084/api/v1/inventory/placements \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Main Banner",
    "publisherId": "pub-1",
    "placementType": "BANNER",
    "pricingModel": "CPC",
    "basePrice": 100
  }' | jq

# 응답에서 "id" 값을 복사 (예: plc-xxxxx)
```

### 7단계: 광고 선택 테스트 (핵심 통합 테스트!) 🎯

```bash
# {placementId}를 위에서 복사한 ID로 교체
curl -X POST http://localhost:8084/api/v1/inventory/select-ad \
  -H "Content-Type: application/json" \
  -d '{
    "placementId": "{placementId}",
    "userContext": {
      "age": 30,
      "gender": "M",
      "country": "KR",
      "city": "Seoul",
      "deviceType": "MOBILE",
      "keywords": ["tech"]
    }
  }' | jq
```

**예상 응답:**
```json
{
  "selectedAd": {
    "campaignId": "camp-xxxxx",
    "adGroupId": "ag-xxxxx",
    "adId": "ad-xxxxx",
    "title": "...",
    "description": "...",
    "landingUrl": "..."
  },
  "matchScore": 85,
  "bid": 500,
  "estimatedCost": 425,
  "impressionToken": "imp-xxxxx"
}
```

### 8단계: 광고주 잔액 확인

```bash
# {advertiserId}를 광고주 ID로 교체
curl http://localhost:8081/api/v1/advertisers/{advertiserId} | jq
```

## 모듈 간 의존성 검증

### Campaign → Advertiser
```bash
# Campaign 생성 시 광고주 존재 여부 자동 확인
# 존재하지 않는 광고주로 캠페인 생성 시도
curl -X POST http://localhost:8082/api/v1/campaigns \
  -H "Content-Type: application/json" \
  -d '{
    "advertiserId": "invalid-adv",
    "name": "Test Campaign",
    "dailyBudget": 10000,
    "totalBudget": 100000,
    "startDate": "2026-01-23",
    "endDate": "2026-12-31"
  }'

# 예상: 404 에러 (광고주를 찾을 수 없습니다)
```

### Inventory → Campaign + Targeting
```bash
# 광고 선택 시 Campaign과 Targeting 서비스 자동 호출
# 위의 7단계 광고 선택 테스트가 이미 검증
```

## 에러 시나리오 테스트

### 1. 잔액 부족 시나리오
```bash
# 광고주 잔액보다 큰 금액 차감 시도
curl -X POST http://localhost:8081/api/v1/advertisers/{advertiserId}/deduct \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 999999999
  }'

# 예상: 400 에러 (잔액이 부족합니다)
```

### 2. 타겟팅 불일치 시나리오
```bash
# 타겟팅 조건과 전혀 맞지 않는 사용자로 광고 선택
curl -X POST http://localhost:8084/api/v1/inventory/select-ad \
  -H "Content-Type: application/json" \
  -d '{
    "placementId": "{placementId}",
    "userContext": {
      "age": 80,
      "gender": "F",
      "country": "US",
      "deviceType": "DESKTOP",
      "keywords": ["sports"]
    }
  }'

# 예상: 404 에러 (사용 가능한 광고가 없습니다) 또는 낮은 matchScore
```

### 3. 비활성 지면 시나리오
```bash
# 지면 비활성화
curl -X PUT http://localhost:8084/api/v1/inventory/placements/{placementId} \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PAUSED"
  }'

# 비활성화된 지면으로 광고 선택 시도
curl -X POST http://localhost:8084/api/v1/inventory/select-ad \
  -H "Content-Type: application/json" \
  -d '{
    "placementId": "{placementId}",
    "userContext": {
      "age": 30,
      "gender": "M",
      "country": "KR",
      "deviceType": "MOBILE"
    }
  }'

# 예상: 400 에러 (비활성화된 지면입니다)
```

## 성능 테스트 (간단)

```bash
# Apache Bench로 광고 선택 API 부하 테스트
ab -n 100 -c 10 -p select_ad_request.json -T application/json \
  http://localhost:8084/api/v1/inventory/select-ad

# select_ad_request.json 파일 내용:
{
  "placementId": "{placementId}",
  "userContext": {
    "age": 30,
    "gender": "M",
    "country": "KR",
    "deviceType": "MOBILE"
  }
}
```

## 데이터 흐름 추적

### 로그 확인
```bash
# 모든 서비스 로그 실시간 확인
docker-compose logs -f

# 특정 서비스 로그만 확인
docker-compose logs -f inventory-service
docker-compose logs -f campaign-service
docker-compose logs -f targeting-service
```

### 데이터베이스 직접 확인
```bash
# PostgreSQL 컨테이너 접속
docker exec -it ad-platform-db psql -U adplatform -d ad_platform

# 광고주 조회
SELECT * FROM advertisers;

# 캠페인 조회
SELECT * FROM campaigns;

# 타겟팅 규칙 조회
SELECT * FROM targeting_rules;

# 지면 조회
SELECT * FROM placements;
```

## 정리

```bash
# 모든 서비스 중지
docker-compose down

# 데이터까지 삭제 (완전 초기화)
docker-compose down -v
```

## 예상 결과

✅ 모든 서비스가 정상 기동  
✅ 광고주 생성 성공  
✅ 캠페인 생성 시 광고주 존재 여부 자동 검증  
✅ 타겟팅 규칙 생성 성공  
✅ 광고 선택 시 Campaign + Targeting 통합 호출  
✅ matchScore 기반 광고 랭킹 정상 작동  
✅ 잔액 부족/비활성 지면 등 에러 처리 정상  

---

**테스트 완료 시각**: 2026-01-22  
**상태**: 4개 모듈 통합 테스트 준비 완료
