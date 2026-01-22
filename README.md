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

## 프로젝트 구조

```
ad-platform-ddd/
├── campaign-module/          # 캠페인 관리 모듈 (구현 완료)
│   ├── domain/               # 도메인 모델 (Campaign, AdGroup, Ad, Budget)
│   ├── application/          # 유스케이스 (CreateCampaign, UpdateStatus, AddAdGroup)
│   ├── infrastructure/       # JPA 엔티티, Repository 구현, Client
│   └── adapter/              # REST Controller, DTO
├── advertiser-module/        # 광고주 관리 (TODO)
├── targeting-module/         # 타겟팅 규칙 (TODO)
├── inventory-module/         # 매체/지면 관리 (TODO)
├── eventlog-module/          # 이벤트 로그 (TODO)
├── metrics-module/           # 성과 집계 (TODO)
├── billing-module/           # 과금 처리 (TODO)
└── api-gateway/              # GraphQL Gateway (TODO)
```

## 빠른 시작

### 1. 사전 요구사항

- Docker & Docker Compose
- Java 17 (로컬 개발 시)
- Gradle 8.5+ (로컬 개발 시)

### 2. Campaign 모듈 실행

```bash
# PostgreSQL + Campaign Service 기동
docker-compose up -d

# 로그 확인
docker-compose logs -f campaign-service

# 서비스 확인
curl http://localhost:8082/api/v1/campaigns?advertiserId=adv-1
```

### 3. 로컬 개발 환경 실행

```bash
# PostgreSQL만 기동
docker-compose up -d postgres

# Campaign 서비스를 로컬에서 실행
cd campaign-module
../gradlew bootRun

# 또는 전체 프로젝트 루트에서
./gradlew :campaign-module:bootRun
```

### 4. 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# Campaign 모듈 테스트만 실행
./gradlew :campaign-module:test

# 특정 테스트 클래스 실행
./gradlew :campaign-module:test --tests CampaignTest
```

## API 사용 예시

### 캠페인 생성

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
  }'
```

**Response:**
```json
{
  "id": "camp-a1b2c3d4",
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
  "endDate": "2026-08-31",
  "createdAt": "2026-01-22T15:30:00Z",
  "updatedAt": "2026-01-22T15:30:00Z"
}
```

### 캠페인 조회

```bash
curl http://localhost:8082/api/v1/campaigns/camp-a1b2c3d4
```

### 캠페인 상태 변경

```bash
curl -X PATCH http://localhost:8082/api/v1/campaigns/camp-a1b2c3d4/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PAUSED"
  }'
```

### 광고그룹 추가

```bash
curl -X POST http://localhost:8082/api/v1/campaigns/camp-a1b2c3d4/ad-groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product Category A",
    "bid": 500
  }'
```

**Response:**
```json
{
  "id": "ag-e5f6g7h8",
  "campaignId": "camp-a1b2c3d4",
  "name": "Product Category A",
  "bid": 500,
  "status": "ACTIVE"
}
```

### 광고주별 캠페인 목록 조회

```bash
curl "http://localhost:8082/api/v1/campaigns?advertiserId=adv-1"
```

## 도메인 모델

### Campaign Aggregate

```
Campaign (Aggregate Root)
├── id: String
├── advertiserId: String
├── name: String
├── budget: Budget (Value Object)
│   ├── dailyBudget: Long
│   ├── totalBudget: Long
│   └── spent: Long
├── status: AdStatus (ACTIVE, PAUSED, DELETED)
├── startDate: LocalDate
├── endDate: LocalDate
└── adGroups: List<AdGroup>
    ├── id: String
    ├── name: String
    ├── bid: Long
    ├── status: AdStatus
    └── ads: List<Ad>
        ├── id: String
        ├── title: String
        ├── description: String
        ├── landingUrl: String
        └── status: AdStatus
```

### 핵심 비즈니스 규칙

1. **예산 관리**
   - 일예산은 총예산을 초과할 수 없음
   - 예산 초과 시 Campaign 자동 PAUSED
   - Budget은 불변 Value Object

2. **상태 관리**
   - DELETED 상태에서는 다른 상태로 전이 불가
   - 캠페인 기간 외에는 ACTIVE 불가

3. **계층 구조**
   - AdGroup은 반드시 Campaign에 속함
   - Ad는 반드시 AdGroup에 속함

## 테스트 전략

### 1. Domain Layer Test (순수 Java)
- 프레임워크 의존성 없음
- 비즈니스 로직 검증
- 예: `CampaignTest`, `BudgetTest`

### 2. Application Layer Test (UseCase)
- Mock을 사용한 단위 테스트
- 외부 의존성 격리
- 예: `CreateCampaignUseCaseTest`

### 3. Adapter Layer Test (Controller)
- `@WebMvcTest`를 사용한 슬라이스 테스트
- HTTP 요청/응답 검증
- 예: `CampaignControllerTest`

## Docker 명령어

```bash
# 전체 서비스 기동
docker-compose up -d

# 특정 서비스만 기동
docker-compose up -d postgres campaign-service

# 로그 확인
docker-compose logs -f campaign-service

# 서비스 중지
docker-compose down

# 볼륨까지 삭제 (DB 데이터 초기화)
docker-compose down -v

# 테스트 환경 기동
docker-compose -f docker-compose.test.yml up -d

# 재빌드 후 기동
docker-compose up -d --build
```

## 개발 워크플로우

### TDD 사이클

1. **Domain Test 작성** → 도메인 모델 구현
2. **UseCase Test 작성** → 유스케이스 구현
3. **Controller Test 작성** → REST API 구현
4. **통합 테스트** → Docker로 검증

### 예시: 새 기능 추가

```bash
# 1. 테스트 작성
vim campaign-module/src/test/java/com/adplatform/campaign/domain/model/CampaignTest.java

# 2. 테스트 실행 (실패 확인)
./gradlew :campaign-module:test --tests CampaignTest

# 3. 구현
vim campaign-module/src/main/java/com/adplatform/campaign/domain/model/Campaign.java

# 4. 테스트 실행 (성공 확인)
./gradlew :campaign-module:test --tests CampaignTest

# 5. 전체 테스트 실행
./gradlew test
```

## 아키텍처 결정 기록 (ADR)

### 왜 Domain과 Infrastructure를 분리했는가?

- **Domain**: 순수 비즈니스 로직, 프레임워크 독립
- **Infrastructure**: JPA, DB 의존성 격리
- **장점**: 테스트 용이성, 도메인 로직 보호, 기술 스택 교체 유연성

### 왜 Aggregate 단위로 Repository를 정의했는가?

- DDD 원칙: Aggregate는 트랜잭션 일관성 경계
- Campaign을 저장하면 AdGroup, Ad도 함께 저장 (Cascade)
- AdGroup만 별도로 조회하는 Repository는 없음

### 왜 Budget을 Value Object로 만들었는가?

- 불변성: 예산 변경 시 새 인스턴스 생성
- 캡슐화: 예산 검증 로직을 Budget 내부에서 처리
- 재사용성: 다른 Aggregate에서도 사용 가능

## 다음 구현 단계

- [ ] Advertiser Module (광고주 잔액 관리)
- [ ] Targeting Module (타겟팅 규칙)
- [ ] Inventory Module (광고 선택 로직)
- [ ] EventLog Module (이벤트 기록)
- [ ] Metrics Module (성과 집계)
- [ ] Billing Module (과금 처리)
- [ ] GraphQL Gateway (통합 조회 API)

## 문의 및 기여

프로젝트 관련 문의나 개선 제안은 Issue를 통해 남겨주세요.

## 라이선스

이 프로젝트는 교육 및 프로토타입 목적으로 제작되었습니다.
