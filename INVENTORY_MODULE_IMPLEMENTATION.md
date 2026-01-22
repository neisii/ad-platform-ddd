# Inventory Module Implementation

## Overview
The Inventory Module is the **core ad selection service** that manages advertising placements and performs intelligent ad selection based on targeting and bidding.

**Port**: 8084  
**Technology Stack**: Spring Boot 3.2.1, Java 17, PostgreSQL, JPA

---

## Architecture

### Domain-Driven Design (DDD) Layers

```
inventory-module/
├── domain/
│   ├── model/              # Domain entities and value objects
│   │   ├── Placement.java  # Aggregate Root
│   │   ├── AdSelection.java
│   │   ├── SelectedAd.java
│   │   ├── PlacementType.java
│   │   ├── PricingModel.java
│   │   └── PlacementStatus.java
│   ├── repository/         # Repository interfaces (Ports)
│   │   └── PlacementRepository.java
│   └── exception/          # Domain exceptions
│       ├── PlacementNotFoundException.java
│       ├── NoAdsAvailableException.java
│       └── InactivePlacementException.java
├── application/
│   ├── usecase/           # Business use cases
│   │   ├── CreatePlacementUseCase.java
│   │   ├── UpdatePlacementUseCase.java
│   │   └── SelectAdUseCase.java (CORE LOGIC)
│   └── dto/               # Application DTOs
│       ├── CreatePlacementCommand.java
│       ├── UpdatePlacementCommand.java
│       ├── SelectAdCommand.java
│       └── AdSelectionResult.java
├── infrastructure/
│   ├── persistence/       # JPA implementation
│   │   ├── PlacementEntity.java
│   │   ├── PlacementJpaRepository.java
│   │   └── PlacementRepositoryImpl.java
│   └── client/           # External service clients
│       ├── CampaignClient.java
│       ├── TargetingClient.java
│       └── DTOs...
└── adapter/
    └── rest/             # REST API
        ├── InventoryController.java
        ├── GlobalExceptionHandler.java
        └── dto/          # Request/Response DTOs
```

---

## Domain Model

### 1. Placement (Aggregate Root)
Represents an advertising placement where ads can be displayed.

**Attributes**:
- `id`: String - Unique identifier
- `name`: String - Placement name
- `publisherId`: String - Publisher who owns this placement
- `placementType`: PlacementType (BANNER/VIDEO/NATIVE)
- `pricingModel`: PricingModel (CPC/CPM/CPA)
- `basePrice`: Long - Minimum price in smallest currency unit
- `status`: PlacementStatus (ACTIVE/PAUSED/DELETED)
- `createdAt`: Instant
- `updatedAt`: Instant

**Invariants**:
- Publisher ID must not be null
- Name must not be empty
- Base price must be >= 0
- Only ACTIVE placements can serve ads

**Business Methods**:
- `update()` - Update placement details
- `activate()` - Activate placement
- `pause()` - Pause placement
- `delete()` - Soft delete
- `canServeAds()` - Check if placement can serve ads

### 2. AdSelection (Value Object)
Represents the result of ad selection process.

**Attributes**:
- `selectedAd`: SelectedAd - Selected ad reference
- `matchScore`: int (0-100) - Targeting match score
- `bid`: long - Campaign bid amount
- `estimatedCost`: long - Estimated cost for this impression
- `impressionToken`: String - Unique tracking token

**Calculated Properties**:
- `rankingScore` = (bid × matchScore) / 100

### 3. SelectedAd (Value Object)
Reference to the selected advertisement.

**Attributes**:
- `campaignId`: String
- `adGroupId`: String
- `adId`: String

### 4. Enums

**PlacementType**:
- `BANNER` - Banner advertisements
- `VIDEO` - Video advertisements
- `NATIVE` - Native advertisements

**PricingModel**:
- `CPC` - Cost Per Click
- `CPM` - Cost Per Mille (1000 impressions)
- `CPA` - Cost Per Action

**PlacementStatus**:
- `ACTIVE` - Active and serving ads
- `PAUSED` - Temporarily disabled
- `DELETED` - Soft deleted

---

## Core Use Cases

### 1. CreatePlacementUseCase
Creates a new advertising placement.

**Input**: CreatePlacementCommand
- name, publisherId, placementType, pricingModel, basePrice

**Process**:
1. Validate command data
2. Create Placement domain object
3. Generate unique ID
4. Save to repository

**Output**: Placement

### 2. UpdatePlacementUseCase
Updates an existing placement.

**Input**: UpdatePlacementCommand
- placementId, name, placementType, pricingModel, basePrice

**Process**:
1. Fetch placement by ID
2. Validate placement exists
3. Update placement using domain method
4. Save changes

**Output**: Placement

**Exceptions**:
- `PlacementNotFoundException` - If placement doesn't exist

### 3. SelectAdUseCase (CORE AD SELECTION LOGIC)

**The heart of the inventory module** - performs intelligent ad selection.

**Input**: SelectAdCommand
- placementId
- userContext (age, gender, country, city, deviceType, keywords)

**Algorithm**:

```
1. VALIDATE PLACEMENT
   - Fetch placement by ID
   - Verify it exists
   - Verify it's ACTIVE status
   
2. FETCH ACTIVE CAMPAIGNS
   - Call Campaign Service REST API
   - Get all campaigns with status=ACTIVE
   
3. MATCH TARGETING FOR EACH CAMPAIGN
   - For each campaign:
     * Call Targeting Service REST API
     * Send campaign ID + user context
     * Receive match score (0-100) and matched flag
   
4. FILTER MATCHED CAMPAIGNS
   - Keep only campaigns where matched=true and score > 0
   
5. CALCULATE RANKING SCORES
   - For each matched campaign:
     * rankingScore = (bid × matchScore) / 100
   
6. SELECT BEST AD
   - Sort by rankingScore (descending)
   - Select campaign with highest score
   
7. CREATE AD SELECTION
   - Generate impression token (UUID)
   - Calculate estimated cost based on pricing model
   - Build AdSelection value object
   
8. RETURN RESULT
```

**Output**: AdSelectionResult
- campaignId, adGroupId, adId, matchScore, bid, estimatedCost, impressionToken

**Exceptions**:
- `PlacementNotFoundException` - Placement doesn't exist
- `InactivePlacementException` - Placement is not active
- `NoAdsAvailableException` - No campaigns available or none matched

---

## REST API Endpoints

### Base URL: `http://localhost:8084/api/v1/inventory`

### 1. Create Placement
```http
POST /api/v1/inventory/placements
Content-Type: application/json

{
  "name": "메인 배너",
  "publisherId": "pub-1",
  "placementType": "BANNER",
  "pricingModel": "CPM",
  "basePrice": 1000
}

Response: 201 Created
{
  "id": "uuid",
  "name": "메인 배너",
  "publisherId": "pub-1",
  "placementType": "BANNER",
  "pricingModel": "CPM",
  "basePrice": 1000,
  "status": "ACTIVE",
  "createdAt": "2025-01-22T...",
  "updatedAt": "2025-01-22T..."
}
```

### 2. Get Placement
```http
GET /api/v1/inventory/placements/{id}

Response: 200 OK
{
  "id": "placement-1",
  "name": "메인 배너",
  ...
}
```

### 3. Update Placement
```http
PUT /api/v1/inventory/placements/{id}
Content-Type: application/json

{
  "name": "업데이트된 배너",
  "placementType": "VIDEO",
  "pricingModel": "CPC",
  "basePrice": 2000
}

Response: 200 OK
```

### 4. Select Ad (CORE ENDPOINT)
```http
POST /api/v1/inventory/select-ad
Content-Type: application/json

{
  "placementId": "placement-1",
  "userContext": {
    "userId": "user-123",
    "age": 25,
    "gender": "M",
    "country": "KR",
    "city": "Seoul",
    "deviceType": "MOBILE",
    "keywords": ["sports", "technology"]
  }
}

Response: 200 OK
{
  "campaignId": "campaign-1",
  "adGroupId": "adgroup-1",
  "adId": "ad-1",
  "matchScore": 85,
  "bid": 5000,
  "estimatedCost": 5,
  "impressionToken": "uuid-campaign-1"
}
```

---

## Integration with Other Services

### Campaign Service (Port 8082)
**Purpose**: Get active campaigns with bid information

**Endpoint Used**:
```
GET http://localhost:8082/api/v1/campaigns/active
```

**Response**:
```json
[
  {
    "id": "campaign-1",
    "advertiserId": "adv-1",
    "name": "Summer Sale",
    "bidAmount": 5000,
    "status": "ACTIVE"
  }
]
```

### Targeting Service (Port 8083)
**Purpose**: Match user context against campaign targeting rules

**Endpoint Used**:
```
POST http://localhost:8083/api/v1/targeting/match
```

**Request**:
```json
{
  "campaignId": "campaign-1",
  "userContext": {
    "age": 25,
    "gender": "M",
    "country": "KR",
    ...
  }
}
```

**Response**:
```json
{
  "campaignId": "campaign-1",
  "matchScore": 85,
  "matched": true
}
```

---

## Configuration

### application.yml
```yaml
server:
  port: 8084

spring:
  application:
    name: inventory-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/ad_platform
    username: adplatform
    password: adplatform123
  
  jpa:
    hibernate:
      ddl-auto: update

services:
  campaign:
    url: http://localhost:8082
  targeting:
    url: http://localhost:8083
```

### Environment Variables
- `SERVER_PORT` - Server port (default: 8084)
- `SPRING_DATASOURCE_URL` - Database URL
- `CAMPAIGN_SERVICE_URL` - Campaign service endpoint
- `TARGETING_SERVICE_URL` - Targeting service endpoint

---

## Testing

### Test Coverage

**Domain Model Tests**:
- ✅ PlacementTest - 9 tests
- ✅ AdSelectionTest - 6 tests
- ✅ SelectedAdTest - 4 tests

**Use Case Tests**:
- ✅ CreatePlacementUseCaseTest - 3 tests
- ✅ UpdatePlacementUseCaseTest - 2 tests
- ✅ SelectAdUseCaseTest - 7 tests (comprehensive scenarios)

**Controller Tests**:
- ✅ InventoryControllerTest - 8 tests

**Total**: 39 tests, all passing ✅

### Running Tests
```bash
# Run all tests
./gradlew :inventory-module:test

# Run with coverage
./gradlew :inventory-module:test jacocoTestReport
```

---

## Ad Selection Algorithm Example

**Scenario**: Select ad for placement-1

**Given**:
- Placement: BANNER, CPM pricing
- User: age=25, gender=M, country=KR, city=Seoul

**Step 1**: Get active campaigns
```
Campaign A: bid=5000
Campaign B: bid=3000
Campaign C: bid=8000
```

**Step 2**: Match targeting
```
Campaign A: matchScore=80, matched=true
Campaign B: matchScore=90, matched=true
Campaign C: matchScore=10, matched=false  (filtered out)
```

**Step 3**: Calculate ranking scores
```
Campaign A: 5000 × 80 / 100 = 4000
Campaign B: 3000 × 90 / 100 = 2700
```

**Step 4**: Select winner
```
Winner: Campaign A (highest ranking score: 4000)
```

**Step 5**: Return result
```json
{
  "campaignId": "campaign-A",
  "matchScore": 80,
  "bid": 5000,
  "estimatedCost": 5,  // CPM: 5000/1000
  "impressionToken": "abc123-campaign-A"
}
```

---

## Key Features

### ✅ Clean Architecture
- Domain logic isolated from infrastructure
- Repository pattern for data access
- Clear separation of concerns

### ✅ Test-Driven Development (TDD)
- Tests written before implementation
- High test coverage (39 tests)
- Unit and integration tests

### ✅ Service Integration
- RESTful client for Campaign service
- RESTful client for Targeting service
- Resilient error handling

### ✅ Business Logic
- Intelligent ad selection algorithm
- Ranking based on bid × match score
- Support for multiple pricing models

### ✅ Production Ready
- Comprehensive error handling
- Validation at all layers
- Logging and monitoring ready
- Transaction management

---

## Database Schema

### placements table
```sql
CREATE TABLE placements (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    publisher_id VARCHAR(255) NOT NULL,
    placement_type VARCHAR(50) NOT NULL,
    pricing_model VARCHAR(50) NOT NULL,
    base_price BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_placements_publisher ON placements(publisher_id);
CREATE INDEX idx_placements_status ON placements(status);
```

---

## Running the Service

### Prerequisites
- Java 17+
- PostgreSQL 14+
- Campaign Service running on port 8082
- Targeting Service running on port 8083

### Start the Service
```bash
# Build
./gradlew :inventory-module:build

# Run
./gradlew :inventory-module:bootRun

# Or with Docker
docker-compose up inventory-service
```

### Health Check
```bash
curl http://localhost:8084/actuator/health
```

---

## Implementation Status

✅ **COMPLETE** - All components implemented and tested

**Completed Components**:
1. ✅ Domain Model (Placement, AdSelection, Enums)
2. ✅ Repository Layer (JPA implementation)
3. ✅ Integration Clients (Campaign, Targeting)
4. ✅ Use Cases (Create, Update, SelectAd)
5. ✅ REST API (All endpoints)
6. ✅ Exception Handling
7. ✅ Configuration
8. ✅ Comprehensive Tests (39 tests passing)

**Test Results**: BUILD SUCCESSFUL ✅

---

## Next Steps / Future Enhancements

1. **Caching**: Add Redis cache for frequently selected ads
2. **Rate Limiting**: Prevent abuse of select-ad endpoint
3. **Analytics**: Track ad selection metrics
4. **A/B Testing**: Support for placement experiments
5. **Real Ad Data**: Fetch actual ad creative from Campaign service
6. **Frequency Capping**: Limit ad impressions per user
7. **Performance**: Optimize for high-throughput ad selection

---

## Contact & Support

For questions or issues related to the Inventory Module, please refer to the main project documentation or contact the development team.

**Module Owner**: Ad Platform Team  
**Last Updated**: January 22, 2025  
**Version**: 1.0.0
