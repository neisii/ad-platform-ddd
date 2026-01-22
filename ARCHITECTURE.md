# Ad Platform DDD - Architecture Documentation

## 프로젝트 개요

Google Ads 스타일 온라인 광고 송출 서비스 프로토타입

**핵심 흐름**: 광고주 → 캠페인 → 광고그룹 → 광고 → 타겟팅 → 이벤트 로그 → 성과 집계 → 과금

**개발 철학**:
- TDD-first
- Tactical DDD (Strategic DDD 미사용)
- Use-case 중심 설계
- Entity는 비즈니스 규칙 소유, Service는 오케스트레이션만
- Repository는 Aggregate 단위로만 정의
- Value Object 적극 활용

## 기술 스택

- Java 17+
- Spring Boot 3.x
- JPA (Hibernate)
- PostgreSQL
- REST + GraphQL
- Gradle
- Docker / docker-compose
- JUnit 5

## 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    Client Applications                       │
└──────────────┬──────────────────────────┬───────────────────┘
               │                          │
        REST API                    GraphQL API
               │                          │
┌──────────────┴──────────────────────────┴───────────────────┐
│                     API Gateway Layer                        │
└───┬──────┬──────┬──────┬──────┬──────┬──────┬──────────────┘
    │      │      │      │      │      │      │
    v      v      v      v      v      v      v
┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
│Adverti-││Campaign││Targeting│Inventory││EventLog││Metrics ││Billing │
│ser     ││        ││        ││        ││        ││        ││        │
│Module  ││Module  ││Module  ││Module  ││Module  ││Module  ││Module  │
└───┬────┘└───┬────┘└───┬────┘└───┬────┘└───┬────┘└───┬────┘└───┬────┘
    │         │         │         │         │         │         │
┌───┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴────┐
│                      PostgreSQL (per module)                         │
└──────────────────────────────────────────────────────────────────────┘
```

## 모듈별 책임

### 1. Advertiser Module
- **책임**: 광고주 계정, 잔액 관리
- **Aggregate Root**: Advertiser
- **핵심 규칙**: 잔액 부족 시 광고 불가

### 2. Campaign Module
- **책임**: 캠페인/광고그룹/광고 생명주기
- **Aggregate Root**: Campaign
- **핵심 규칙**: 예산 초과 시 PAUSED, 계층 구조 일관성

### 3. Targeting Module
- **책임**: 타겟팅 조건 관리
- **Aggregate Root**: TargetingRule
- **핵심 규칙**: 조건 매칭 로직 캡슐화

### 4. Inventory Module
- **책임**: 매체/지면 관리, 단가 정책
- **Aggregate Root**: Placement
- **핵심 규칙**: CPC/CPM 단가 계산

### 5. EventLog Module
- **책임**: 노출/클릭/전환 이벤트 기록
- **Aggregate Root**: AdEvent
- **핵심 규칙**: Append-only, 불변

### 6. Metrics Module
- **책임**: 성과 집계 (일자별)
- **Aggregate Root**: DailyMetrics
- **핵심 규칙**: CTR/CVR/CPA 계산

### 7. Billing Module
- **책임**: 비용 차감, 과금
- **Aggregate Root**: BillingTransaction
- **핵심 규칙**: 이중 과금 방지

## Campaign Module 상세 설계

### Aggregate 구조

```
Campaign (Aggregate Root)
├── id: String
├── advertiserId: String
├── name: String
├── budget: Budget (Value Object)
├── status: AdStatus (Value Object)
├── startDate: LocalDate
├── endDate: LocalDate
├── adGroups: List<AdGroup> (Entity)
│   ├── id: String
│   ├── campaignId: String
│   ├── name: String
│   ├── bid: Long
│   ├── status: AdStatus
│   └── ads: List<Ad> (Entity)
│       ├── id: String
│       ├── adGroupId: String
│       ├── title: String
│       ├── description: String
│       ├── landingUrl: String
│       └── status: AdStatus
└── createdAt: Instant
```

### Value Objects

**Budget**:
- dailyBudget: Long
- totalBudget: Long
- spent: Long
- 불변성 보장
- 예산 검증 로직 캡슐화

**AdStatus** (enum):
- ACTIVE
- PAUSED
- DELETED

### 비즈니스 규칙

1. **예산 관리**
   - 일예산은 총예산을 초과할 수 없음
   - 예산 초과 시 Campaign은 자동으로 PAUSED
   - 지출 기록 시 잔여 예산 계산

2. **상태 전이**
   - DELETED 상태에서는 다른 상태로 전이 불가
   - 캠페인 기간 외에는 ACTIVE 상태 불가

3. **계층 구조**
   - AdGroup은 반드시 Campaign에 속함
   - Ad는 반드시 AdGroup에 속함
   - 부모의 PAUSED/DELETED 상태는 자식에게 전파

### 레이어 구조

```
campaign-module/
├── domain/
│   ├── model/
│   │   ├── Campaign.java (Aggregate Root)
│   │   ├── AdGroup.java (Entity)
│   │   ├── Ad.java (Entity)
│   │   ├── Budget.java (Value Object)
│   │   └── AdStatus.java (Enum)
│   ├── repository/
│   │   └── CampaignRepository.java (Interface)
│   └── exception/
│       ├── CampaignNotFoundException.java
│       ├── CampaignDateRangeException.java
│       └── BudgetExceededException.java
├── application/
│   ├── usecase/
│   │   ├── CreateCampaignUseCase.java
│   │   ├── UpdateCampaignStatusUseCase.java
│   │   ├── AddAdGroupUseCase.java
│   │   └── RecordCampaignSpentUseCase.java
│   └── dto/
│       ├── CreateCampaignCommand.java
│       ├── UpdateCampaignStatusCommand.java
│       └── CampaignResponse.java
├── infrastructure/
│   ├── persistence/
│   │   ├── CampaignJpaRepository.java
│   │   ├── CampaignEntity.java
│   │   ├── AdGroupEntity.java
│   │   ├── AdEntity.java
│   │   └── CampaignRepositoryImpl.java
│   └── client/
│       ├── AdvertiserClient.java
│       └── AdvertiserClientImpl.java
└── adapter/
    ├── rest/
    │   ├── CampaignController.java
    │   └── request/response DTOs
    └── graphql/
        ├── CampaignQueryResolver.java
        └── types/
```

## Event → Metrics → Billing 흐름

```
Ad Request → Inventory → Campaign (budget check) → Targeting (match) → Ad Selection
     ↓
Event (Impression/Click/Conversion)
     ↓
EventLog (Append-only write)
     ↓
Metrics Aggregation Job (Scheduled)
     ↓
DailyMetrics (일자별 집계)
     ↓
Billing (비용 차감)
     ↓
Advertiser Balance Update
```

## REST API 설계

### Campaign API

```
POST   /api/v1/campaigns
PATCH  /api/v1/campaigns/{campaignId}/status
GET    /api/v1/campaigns/{campaignId}
POST   /api/v1/campaigns/{campaignId}/ad-groups
POST   /api/v1/campaigns/{campaignId}/ad-groups/{adGroupId}/ads
```

## GraphQL Schema 핵심

```graphql
type Campaign {
  id: ID!
  advertiserId: ID!
  name: String!
  status: AdStatus!
  budget: Budget!
  startDate: String!
  endDate: String!
  adGroups: [AdGroup!]!
  metrics(startDate: String!, endDate: String!): CampaignMetricsSummary!
}
```

## TDD 작성 순서

1. **Domain Test** (순수 Java, Spring/DB 의존 없음)
   - CampaignTest.java
   - BudgetTest.java
   - AdGroupTest.java

2. **UseCase / Application Service Test**
   - CreateCampaignUseCaseTest.java
   - UpdateCampaignStatusUseCaseTest.java

3. **Adapter Test** (REST / GraphQL / Repository)
   - CampaignControllerTest.java
   - CampaignRepositoryImplTest.java

## Docker 구성

각 모듈은 독립 Spring Boot Application으로 실행 가능:

```bash
# 전체 기동
docker-compose up -d

# 특정 모듈만 기동
docker-compose up -d postgres campaign-service

# 테스트 환경
docker-compose -f docker-compose.test.yml up -d
```

## 다음 구현 단계

1. Campaign Module 전체 레이어 구현 (TDD)
2. Advertiser Module 구현
3. Targeting Module 구현
4. Inventory Module 구현
5. EventLog Module 구현
6. Metrics Module 구현
7. Billing Module 구현
8. API Gateway 구성
9. Docker 통합

---

**문서 작성일**: 2026-01-22
**프로젝트 상태**: 설계 완료, 구현 시작 전
