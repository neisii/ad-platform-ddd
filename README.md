# Ad Platform DDD

**Domain-driven, test-first online advertising platform prototype implementing the full advertiser → campaign → ad group → ad → targeting → event → metrics → billing flow with modular Docker-based architecture.**

## 프로젝트 개요

Google Ads 스타일의 온라인 광고 송출 서비스 프로토타입입니다.

### 핵심 특징

- **TDD-first**: 모든 도메인 로직은 테스트로 먼저 검증
- **Tactical DDD**: Aggregate, Entity, Value Object, Repository 패턴 적용
- **Use-case 중심**: 비즈니스 유스케이스 명확히 분리
- **모듈별 독립 실행**: Docker Compose로 필요한 모듈만 선택적 기동
- **계층형 아키텍처**: Domain → Application → Infrastructure → Adapter

## 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.1
- **ORM**: JPA (Hibernate)
- **Database**: PostgreSQL 15
- **Build**: Gradle 8.5
- **Container**: Docker & Docker Compose
- **Test**: JUnit 5, AssertJ, Mockito

## 구현 완료된 모듈 ✅

### 1. Advertiser Module (Port 8081)
- 광고주 계정 관리
- 잔액 충전/차감
- 상태 관리 (ACTIVE/SUSPENDED/DELETED)
- **테스트**: 21+ cases, 100% pass

### 2. Campaign Module (Port 8082)
- 캠페인/광고그룹/광고 계층 관리
- 예산 관리 (일예산/총예산)
- 예산 초과 시 자동 PAUSED
- **테스트**: 47 cases, 100% pass

### 3. Targeting Module (Port 8083)
- 타겟팅 규칙 관리
- Demographics (나이/성별)
- 지역/디바이스/키워드 타겟팅
- 매칭 스코어 계산 (0-100)
- **테스트**: 45 cases, 100% pass

### 4. Inventory Module (Port 8084)
- 광고 지면 관리
- **핵심 광고 선택 로직**
- 입찰가 × 매칭 스코어 기반 랭킹
- CPC/CPM/CPA 가격 모델
- **테스트**: 39 cases, 100% pass

**총 테스트**: 152+ cases, 100% pass rate ✅

## 프로젝트 구조

```
ad-platform-ddd/
├── advertiser-module/        # 광고주 관리 (✅ 구현 완료)
├── campaign-module/          # 캠페인 관리 (✅ 구현 완료)
├── targeting-module/         # 타겟팅 (✅ 구현 완료)
├── inventory-module/         # 광고 선택 (✅ 구현 완료)
├── eventlog-module/          # 이벤트 로그 (TODO)
├── metrics-module/           # 성과 집계 (TODO)
├── billing-module/           # 과금 처리 (TODO)
└── api-gateway/              # GraphQL Gateway (TODO)
```

## 빠른 시작

### 1. 전체 시스템 기동

```bash
# 모든 서비스 기동 (PostgreSQL + 4개 서비스)
docker-compose up -d

# 서비스 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f
```

### 2. 전체 플로우 테스트

#### 2-1. 광고주 생성
```bash
curl -X POST http://localhost:8081/api/v1/advertisers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Company",
    "email": "test@example.com",
    "initialBalance": 1000000
  }' | jq

# 응답의 id를 복사 (예: adv-xxxxx)
```

#### 2-2. 캠페인 생성
```bash
curl -X POST http://localhost:8082/api/v1/campaigns \
  -H "Content-Type: application/json" \
  -d '{
    "advertiserId": "adv-xxxxx",
    "name": "Summer Sale 2026",
    "dailyBudget": 100000,
    "totalBudget": 3000000,
    "startDate": "2026-01-23",
    "endDate": "2026-12-31"
  }' | jq

# 응답의 id를 복사 (예: camp-xxxxx)
```

#### 2-3. 광고그룹 추가
```bash
curl -X POST http://localhost:8082/api/v1/campaigns/camp-xxxxx/ad-groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product Category A",
    "bid": 500
  }' | jq
```

#### 2-4. 타겟팅 규칙 생성
```bash
curl -X POST http://localhost:8083/api/v1/targeting/rules \
  -H "Content-Type: application/json" \
  -d '{
    "campaignId": "camp-xxxxx",
    "ageMin": 20,
    "ageMax": 40,
    "gender": "M",
    "geoTargets": ["KR", "Seoul"],
    "deviceTypes": ["MOBILE"],
    "keywords": ["tech", "gaming"]
  }' | jq
```

#### 2-5. 광고 지면 생성
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

# 응답의 id를 복사 (예: plc-xxxxx)
```

#### 2-6. 광고 선택 (핵심 통합!) 🎯
```bash
curl -X POST http://localhost:8084/api/v1/inventory/select-ad \
  -H "Content-Type: application/json" \
  -d '{
    "placementId": "plc-xxxxx",
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
    "adId": "ad-xxxxx"
  },
  "matchScore": 85,
  "bid": 500,
  "estimatedCost": 425,
  "impressionToken": "imp-xxxxx"
}
```

## API 엔드포인트

### Advertiser Service (8081)
- `POST /api/v1/advertisers` - 광고주 생성
- `GET /api/v1/advertisers/{id}` - 광고주 조회
- `POST /api/v1/advertisers/{id}/charge` - 잔액 충전
- `POST /api/v1/advertisers/{id}/deduct` - 잔액 차감
- `GET /api/v1/advertisers/{id}/exists` - 존재 확인

### Campaign Service (8082)
- `POST /api/v1/campaigns` - 캠페인 생성
- `GET /api/v1/campaigns/{id}` - 캠페인 조회
- `PATCH /api/v1/campaigns/{id}/status` - 상태 변경
- `POST /api/v1/campaigns/{id}/ad-groups` - 광고그룹 추가
- `GET /api/v1/campaigns?advertiserId={id}` - 목록 조회

### Targeting Service (8083)
- `POST /api/v1/targeting/rules` - 타겟팅 규칙 생성
- `GET /api/v1/targeting/rules/{id}` - 규칙 조회
- `GET /api/v1/targeting/rules/campaign/{id}` - 캠페인별 조회
- `POST /api/v1/targeting/match` - 사용자 매칭
- `PUT /api/v1/targeting/rules/{id}` - 규칙 수정

### Inventory Service (8084)
- `POST /api/v1/inventory/placements` - 지면 생성
- `GET /api/v1/inventory/placements/{id}` - 지면 조회
- `PUT /api/v1/inventory/placements/{id}` - 지면 수정
- `POST /api/v1/inventory/select-ad` - 광고 선택 ⭐

## 핵심 비즈니스 로직

### 광고 선택 알고리즘 (Inventory)
```
1. 지면 유효성 검증 (활성 상태)
2. Campaign Service에서 활성 캠페인 조회
3. Targeting Service로 사용자 매칭
4. 랭킹 계산: (입찰가 × 매칭스코어) / 100
5. 최고 랭킹 광고 선택
6. Impression Token 생성
```

### 타겟팅 매칭 스코어
```
- Demographics: 30점 (나이 범위 + 성별)
- Geography: 25점 (국가/도시)
- Device Type: 20점
- Keywords: 25점 (부분 매칭)

총점: 0-100점 (설정된 조건만 계산)
```

### 예산 관리 (Campaign)
```
- 일예산/총예산 검증
- 지출 기록 시 실시간 잔여 예산 계산
- 예산 초과 시 자동 PAUSED
- Budget은 불변 Value Object
```

## 로컬 개발 환경

```bash
# PostgreSQL만 기동
docker-compose up -d postgres

# 특정 서비스를 로컬에서 실행
./gradlew :advertiser-module:bootRun
./gradlew :campaign-module:bootRun
./gradlew :targeting-module:bootRun
./gradlew :inventory-module:bootRun
```

## 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :advertiser-module:test
./gradlew :campaign-module:test
./gradlew :targeting-module:test
./gradlew :inventory-module:test

# 테스트 리포트
open */build/reports/tests/test/index.html
```

## 아키텍처 하이라이트

### 1. 계층형 아키텍처
```
Adapter (REST)
    ↓
Application (UseCase)
    ↓
Domain (Business Logic)
    ↓
Infrastructure (Persistence)
```

### 2. 모듈 간 통신
```
Inventory → Campaign (활성 캠페인 조회)
Inventory → Targeting (사용자 매칭)
Campaign → Advertiser (광고주 존재 확인)
```

### 3. DDD 패턴
- **Aggregate Root**: Advertiser, Campaign, TargetingRule, Placement
- **Entity**: AdGroup, Ad
- **Value Object**: Money, Budget, Demographics, UserContext, AdSelection

### 4. 테스트 전략
- **Domain Test**: 순수 Java, 프레임워크 독립
- **UseCase Test**: Mock 사용, 의존성 격리
- **Controller Test**: @WebMvcTest, 슬라이스 테스트

## Docker 명령어

```bash
# 전체 서비스 기동
docker-compose up -d

# 특정 서비스만 기동
docker-compose up -d postgres advertiser-service

# 로그 확인
docker-compose logs -f inventory-service

# 서비스 중지
docker-compose down

# 데이터까지 삭제
docker-compose down -v

# 재빌드 후 기동
docker-compose up -d --build
```

## 문서

- **ARCHITECTURE.md** - 전체 아키텍처 설계
- **QUICKSTART.md** - 5분 시작 가이드
- **INTEGRATION_TEST_GUIDE.md** - 통합 테스트 가이드
- **IMPLEMENTATION_SUMMARY.md** - 구현 상세
- **PROJECT_STATUS.md** - 현황 통계

## 구현 완료 현황

| 모듈 | 상태 | 테스트 | 포트 |
|------|------|--------|------|
| Advertiser | ✅ 완료 | 21+ cases | 8081 |
| Campaign | ✅ 완료 | 47 cases | 8082 |
| Targeting | ✅ 완료 | 45 cases | 8083 |
| Inventory | ✅ 완료 | 39 cases | 8084 |
| EventLog | 🔜 예정 | - | 8085 |
| Metrics | 🔜 예정 | - | 8086 |
| Billing | 🔜 예정 | - | 8087 |
| API Gateway | 🔜 예정 | - | 8080 |

## 다음 구현 단계

1. **EventLog Module** - 이벤트 기록 (Impression/Click/Conversion)
2. **Metrics Module** - 성과 집계 (CTR/CVR/CPA)
3. **Billing Module** - 비용 차감 및 과금
4. **GraphQL Gateway** - 통합 조회 API

## 문의 및 기여

- GitHub: https://github.com/neisii/ad-platform-ddd
- Issues: https://github.com/neisii/ad-platform-ddd/issues

## 라이선스

이 프로젝트는 교육 및 프로토타입 목적으로 제작되었습니다.

---

**마지막 업데이트**: 2026-01-22  
**구현 완료**: 4/8 모듈 (Advertiser, Campaign, Targeting, Inventory)  
**테스트 통과**: 152+ cases, 100% ✅
