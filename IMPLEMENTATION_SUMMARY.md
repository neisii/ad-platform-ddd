# Campaign Module 구현 완료 요약

## 구현 완료 항목

### ✅ 1. 프로젝트 구조 및 설정
- [x] Gradle 멀티 모듈 프로젝트 구성
- [x] Campaign 모듈 디렉토리 구조 생성
- [x] Spring Boot 3.2.1 설정
- [x] PostgreSQL 연동 설정
- [x] Docker & Docker Compose 설정

### ✅ 2. Domain Layer (순수 비즈니스 로직)

#### Value Objects
- **Budget** (예산)
  - 일예산/총예산 검증
  - 지출 기록 (불변성)
  - 예산 초과 여부 판단
  - 8개 테스트 케이스 작성 ✅

- **AdStatus** (상태)
  - ACTIVE, PAUSED, DELETED
  - 상태 전이 규칙

#### Entities
- **Ad** (광고)
  - 제목, 설명, 랜딩 URL 관리
  - URL 유효성 검증
  - 상태 관리
  - 6개 테스트 케이스 작성 ✅

- **AdGroup** (광고그룹)
  - 입찰가 관리
  - Ad 목록 관리
  - 활성 광고 확인
  - 8개 테스트 케이스 작성 ✅

#### Aggregate Root
- **Campaign** (캠페인)
  - 예산 초과 시 자동 PAUSED
  - 캠페인 기간 검증
  - AdGroup 계층 관리
  - 활성 상태 종합 판단
  - 11개 테스트 케이스 작성 ✅

**총 Domain 테스트: 33개 ✅**

### ✅ 3. Application Layer (Use Cases)

- **CreateCampaignUseCase**
  - 광고주 존재 여부 확인
  - 캠페인 생성 및 저장
  - ID 자동 생성
  - 3개 테스트 케이스 작성 ✅

- **UpdateCampaignStatusUseCase**
  - 캠페인 조회
  - 상태 변경 및 저장
  - 2개 테스트 케이스 작성 ✅

- **AddAdGroupUseCase**
  - 캠페인 조회
  - AdGroup 생성 및 추가
  - ID 자동 생성
  - 3개 테스트 케이스 작성 ✅

**총 Application 테스트: 8개 ✅**

### ✅ 4. Infrastructure Layer (영속성)

#### JPA Entities
- **CampaignEntity**
  - OneToMany with AdGroupEntity
  - Cascade ALL, orphanRemoval
  - 생성/수정 시간 자동 관리

- **AdGroupEntity**
  - ManyToOne with CampaignEntity
  - OneToMany with AdEntity

- **AdEntity**
  - ManyToOne with AdGroupEntity

#### Repository
- **CampaignJpaRepository**
  - Spring Data JPA 인터페이스
  - 광고주 ID로 조회 (Fetch Join 최적화)

- **CampaignRepositoryImpl**
  - Domain Repository 인터페이스 구현
  - Entity ↔ Domain Model 변환
  - 업데이트 로직 최적화

#### Mapper
- **CampaignMapper**
  - Domain Model ↔ JPA Entity 양방향 변환
  - 계층 구조 재귀 변환 (Campaign → AdGroup → Ad)

#### External Client
- **AdvertiserClientImpl**
  - 프로토타입: Mock 구현 (adv- prefix 검증)
  - 실제 환경: REST 호출로 대체 가능

### ✅ 5. Adapter Layer (REST API)

#### DTOs
- **CreateCampaignRequest** - Validation 포함
- **CampaignResponse** - Budget 정보 포함
- **UpdateCampaignStatusRequest**
- **AddAdGroupRequest**
- **AdGroupResponse**

#### Controller
- **CampaignController**
  - POST /api/v1/campaigns - 캠페인 생성
  - GET /api/v1/campaigns/{id} - 캠페인 조회
  - GET /api/v1/campaigns?advertiserId={id} - 목록 조회
  - PATCH /api/v1/campaigns/{id}/status - 상태 변경
  - POST /api/v1/campaigns/{id}/ad-groups - 광고그룹 추가

#### Exception Handling
- **GlobalExceptionHandler**
  - CampaignNotFoundException → 404
  - AdvertiserNotFoundException → 404
  - CampaignDateRangeException → 400
  - MethodArgumentNotValidException → 400
  - IllegalArgumentException → 400
  - IllegalStateException → 409
  - Exception → 500

**총 Controller 테스트: 6개 ✅**

### ✅ 6. Docker & 배포 설정

- **Dockerfile** (Multi-stage build)
  - Builder: Gradle로 bootJar 생성
  - Runtime: JRE만 포함하여 경량화
  - Health check 설정

- **docker-compose.yml**
  - PostgreSQL 서비스
  - Campaign 서비스
  - 네트워크 및 볼륨 설정
  - Health check 의존성

- **docker-compose.test.yml**
  - 테스트 환경 전용
  - 독립적인 DB 및 네트워크

## 테스트 커버리지 요약

| 레이어 | 테스트 클래스 수 | 테스트 케이스 수 | 상태 |
|--------|------------------|------------------|------|
| Domain | 4 | 33 | ✅ 통과 |
| Application | 3 | 8 | ✅ 통과 |
| Adapter | 1 | 6 | ✅ 통과 |
| **총합** | **8** | **47** | **✅ 통과** |

## 실행 방법

### 1. Docker로 전체 실행
```bash
docker-compose up -d
curl http://localhost:8082/api/v1/campaigns?advertiserId=adv-1
```

### 2. 로컬 개발 환경
```bash
# PostgreSQL만 기동
docker-compose up -d postgres

# Campaign 서비스 실행
./gradlew :campaign-module:bootRun
```

### 3. 테스트 실행
```bash
./gradlew :campaign-module:test
```

### 4. 빌드
```bash
./gradlew :campaign-module:build
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

### 광고그룹 추가
```bash
curl -X POST http://localhost:8082/api/v1/campaigns/{campaignId}/ad-groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product Category A",
    "bid": 500
  }'
```

## 아키텍처 하이라이트

### 1. 도메인 중심 설계
- **Domain Layer는 프레임워크에 독립적**
  - Spring, JPA 의존성 없음
  - 순수 Java로 비즈니스 로직 구현
  - 빠른 단위 테스트 가능

### 2. Aggregate 패턴 적용
- **Campaign이 Aggregate Root**
  - AdGroup, Ad는 Campaign을 통해서만 접근
  - 트랜잭션 일관성 보장
  - Repository는 Campaign 단위로만 제공

### 3. Value Object 활용
- **Budget은 불변 객체**
  - 지출 기록 시 새 인스턴스 생성
  - 예산 검증 로직 캡슐화
  - 도메인 규칙을 코드로 표현

### 4. 계층 분리
```
Adapter (REST)
    ↓
Application (UseCase)
    ↓
Domain (Business Logic)
    ↓
Infrastructure (Persistence)
```

### 5. 테스트 전략
- **Domain Test**: 순수 Java, 프레임워크 없음
- **UseCase Test**: Mock 사용, 외부 의존성 격리
- **Controller Test**: @WebMvcTest, 슬라이스 테스트

## 비즈니스 규칙 구현

### 예산 관리
✅ 일예산은 총예산을 초과할 수 없음
✅ 예산 초과 시 Campaign 자동 PAUSED
✅ 지출 기록 시 잔여 예산 실시간 계산

### 상태 관리
✅ DELETED 상태에서는 다른 상태로 전이 불가
✅ 캠페인 기간 외에는 ACTIVE 불가
✅ 활성 상태는 상태 + 기간 + 예산 모두 만족해야 함

### 계층 구조
✅ AdGroup은 반드시 Campaign에 속함
✅ Ad는 반드시 AdGroup에 속함
✅ 부모 저장 시 자식도 함께 저장 (Cascade)

## 다음 구현 단계 (TODO)

### 1단계: 핵심 모듈
- [ ] Advertiser Module (광고주 잔액 관리)
- [ ] Targeting Module (타겟팅 규칙 매칭)
- [ ] Inventory Module (광고 선택 로직)

### 2단계: 이벤트 & 집계
- [ ] EventLog Module (노출/클릭/전환 기록)
- [ ] Metrics Module (일자별 성과 집계)
- [ ] Billing Module (비용 차감 및 과금)

### 3단계: 통합 API
- [ ] GraphQL Gateway (통합 조회 API)
- [ ] 모듈 간 REST 통신 구현
- [ ] Event-driven 아키텍처 적용 (선택)

## 학습 포인트

### TDD의 장점
1. **빠른 피드백**: 도메인 로직 변경 시 즉시 검증
2. **리팩토링 안전성**: 테스트가 안전망 역할
3. **문서화**: 테스트 코드가 사용 예시 문서

### DDD의 장점
1. **유비쿼터스 언어**: Campaign, Budget, AdStatus 등 도메인 용어 사용
2. **경계 명확화**: Aggregate로 트랜잭션 경계 설정
3. **비즈니스 로직 보호**: Domain Layer가 순수하게 유지됨

### 계층형 아키텍처의 장점
1. **관심사 분리**: 각 레이어가 명확한 책임
2. **테스트 용이성**: 레이어별 독립 테스트
3. **유연성**: Infrastructure 변경(JPA → MyBatis)이 Domain에 영향 없음

---

**구현 완료일**: 2026-01-22
**작성자**: Campaign Module Implementation Team
**상태**: ✅ 프로덕션 준비 완료 (프로토타입)
