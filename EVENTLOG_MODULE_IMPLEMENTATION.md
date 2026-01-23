# EventLog Module - Complete Implementation

## Overview
The EventLog module is a fully functional, production-ready service for tracking ad events (impressions, clicks, conversions) following Domain-Driven Design (DDD) and Test-Driven Development (TDD) principles.

## Architecture

### Domain Layer (Core Business Logic)

#### 1. EventType Enum
- **Location**: `/eventlog-module/src/main/java/com/adplatform/eventlog/domain/model/EventType.java`
- **Values**: IMPRESSION, CLICK, CONVERSION
- **Purpose**: Type-safe event classification

#### 2. AdEvent (Aggregate Root)
- **Location**: `/eventlog-module/src/main/java/com/adplatform/eventlog/domain/model/AdEvent.java`
- **Immutable**: Events cannot be modified after creation (append-only)
- **Fields**:
  - `id`: Unique event identifier
  - `eventType`: IMPRESSION/CLICK/CONVERSION
  - `adId`, `campaignId`, `adGroupId`: Ad hierarchy references
  - `userId`, `sessionId`: User tracking
  - `timestamp`: Event occurrence time (Instant)
  - `metadata`: Flexible Map<String, String> for additional data
  - `impressionToken`: Links clicks/conversions to impressions

#### 3. Business Rules (Validated in Domain)
✅ Events are immutable (no setters)
✅ Required fields: eventType, adId, campaignId, timestamp
✅ CLICK/CONVERSION must have impressionToken
✅ Timestamp cannot be in the future
✅ Metadata is immutable (UnmodifiableMap)
✅ Duplicate prevention via unique ID

#### 4. EventRepository Interface (Port)
- **Location**: `/eventlog-module/src/main/java/com/adplatform/eventlog/domain/repository/EventRepository.java`
- **Methods**:
  - `save()`: Store new event (idempotent)
  - `findById()`: Retrieve by ID
  - `findByAdId()`: Query events for specific ad
  - `findByTimeRange()`: Query events by timestamp range
  - `existsById()`: Check for duplicates

### Application Layer (Use Cases)

#### 1. RecordEventUseCase
- **Location**: `/eventlog-module/src/main/java/com/adplatform/eventlog/application/usecase/RecordEventUseCase.java`
- **Purpose**: Record new ad events with idempotent behavior
- **Features**:
  - Checks for duplicate IDs before saving
  - Returns existing event if duplicate detected
  - Validates business rules via domain model
  - Transactional processing

#### 2. GetEventsByAdUseCase
- **Location**: `/eventlog-module/src/main/java/com/adplatform/eventlog/application/usecase/GetEventsByAdUseCase.java`
- **Purpose**: Retrieve all events for a specific ad
- **Validation**: Ensures adId is not null or empty

#### 3. GetEventsByTimeRangeUseCase
- **Location**: `/eventlog-module/src/main/java/com/adplatform/eventlog/application/usecase/GetEventsByTimeRangeUseCase.java`
- **Purpose**: Query events within a time range
- **Validation**: 
  - Both startTime and endTime required
  - startTime must be before endTime

#### DTOs
- **RecordEventCommand**: Input for recording events
- **EventResult**: Use case output
- **Conversions**: EventResult.from(AdEvent)

### Infrastructure Layer (Technical Details)

#### 1. AdEventEntity (JPA)
- **Location**: `/eventlog-module/src/main/java/com/adplatform/eventlog/infrastructure/persistence/AdEventEntity.java`
- **Features**:
  - `@Immutable`: Hibernate optimization for read-only entities
  - `@Table` with indexes on: ad_id, timestamp, campaign_id
  - `@ElementCollection` for metadata storage
  - All columns marked `updatable = false`

#### 2. AdEventJpaRepository
- **Location**: `/eventlog-module/src/main/java/com/adplatform/eventlog/infrastructure/persistence/AdEventJpaRepository.java`
- **Extends**: JpaRepository<AdEventEntity, String>
- **Custom Queries**:
  - `findByAdId()`
  - `findByTimestampBetween()` with ORDER BY
  - `findByCampaignId()`
  - `findByEventType()`

#### 3. EventRepositoryImpl
- **Location**: `/eventlog-module/src/main/java/com/adplatform/eventlog/infrastructure/persistence/EventRepositoryImpl.java`
- **Implements**: EventRepository (domain interface)
- **Pattern**: Adapter pattern (Infrastructure → Domain)
- **Conversions**: Entity ↔ Domain model

#### Database Schema
```sql
-- Main events table
CREATE TABLE ad_events (
    id VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    ad_id VARCHAR(255) NOT NULL,
    campaign_id VARCHAR(255) NOT NULL,
    ad_group_id VARCHAR(255),
    user_id VARCHAR(255),
    session_id VARCHAR(255),
    timestamp TIMESTAMP NOT NULL,
    impression_token VARCHAR(255)
);

-- Metadata table (key-value pairs)
CREATE TABLE ad_event_metadata (
    event_id VARCHAR(255) NOT NULL,
    meta_key VARCHAR(255) NOT NULL,
    meta_value TEXT,
    PRIMARY KEY (event_id, meta_key),
    FOREIGN KEY (event_id) REFERENCES ad_events(id)
);

-- Performance indexes
CREATE INDEX idx_ad_id ON ad_events(ad_id);
CREATE INDEX idx_timestamp ON ad_events(timestamp);
CREATE INDEX idx_campaign_id ON ad_events(campaign_id);
```

### Adapter Layer (REST API)

#### EventLogController
- **Location**: `/eventlog-module/src/main/java/com/adplatform/eventlog/adapter/rest/EventLogController.java`
- **Base Path**: `/api/v1/events`

**Endpoints**:

1. **POST /api/v1/events** - Record event
   - Returns: 202 Accepted
   - Body: RecordEventRequest
   - Validates with Jakarta Bean Validation

2. **GET /api/v1/events/ad/{adId}** - Get events by ad
   - Returns: List<EventResponse>
   - Query all events for specific ad

3. **GET /api/v1/events?startTime={iso}&endTime={iso}** - Get by time range
   - Returns: List<EventResponse>
   - ISO-8601 timestamp format

#### DTOs
- **RecordEventRequest**: Input validation with @NotNull, @NotBlank
- **EventResponse**: Output format
- **GlobalExceptionHandler**: Centralized error handling

## Testing

### Test Coverage Summary

#### 1. Domain Model Tests (19 tests)
- **File**: `AdEventTest.java`
- **Coverage**:
  - ✅ Valid event creation (IMPRESSION, CLICK, CONVERSION)
  - ✅ Metadata handling and immutability
  - ✅ All validation rules (null checks, required fields)
  - ✅ Business rule enforcement (impressionToken for CLICK/CONVERSION)
  - ✅ Timestamp validation (no future dates)

#### 2. Use Case Tests (13 tests)
- **Files**: 
  - `RecordEventUseCaseTest.java` (4 tests)
  - `GetEventsByAdUseCaseTest.java` (4 tests)
  - `GetEventsByTimeRangeUseCaseTest.java` (5 tests)
- **Coverage**:
  - ✅ Successful operations
  - ✅ Idempotent behavior for duplicate events
  - ✅ Validation error handling
  - ✅ Empty result scenarios
  - ✅ Edge cases (null, empty, invalid ranges)

#### 3. Infrastructure Tests (8 tests)
- **File**: `EventRepositoryImplTest.java`
- **Type**: Integration tests with H2 database
- **Coverage**:
  - ✅ Save and retrieve events
  - ✅ Metadata persistence
  - ✅ Query by ad ID
  - ✅ Query by time range
  - ✅ Existence checks
  - ✅ All event types (IMPRESSION, CLICK, CONVERSION)
  - ✅ Complete event with all fields

#### 4. REST API Tests (11 tests)
- **File**: `EventLogControllerTest.java`
- **Type**: Integration tests with MockMvc
- **Coverage**:
  - ✅ POST endpoint for all event types
  - ✅ Metadata in requests
  - ✅ Idempotent duplicate handling
  - ✅ Validation error responses (400)
  - ✅ GET by ad ID
  - ✅ GET by time range
  - ✅ Empty result handling
  - ✅ Invalid request handling

**Total: 51 tests, ALL PASSING ✅**

## Configuration

### Application Properties
- **File**: `/eventlog-module/src/main/resources/application.yml`
- **Server Port**: 8085
- **Database**: PostgreSQL
  - URL: `jdbc:postgresql://localhost:5432/ad_platform_eventlog`
  - Connection pool: HikariCP (max 10, min 5)
- **JPA**: Hibernate with PostgreSQL dialect
- **Logging**: INFO level for application, WARN for Hibernate

### Docker Configuration
- **Dockerfile**: `/eventlog-module/Dockerfile`
- **Container**: eventlog-service
- **Port Mapping**: 8085:8085
- **Dependencies**: PostgreSQL database
- **Network**: ad-platform-network

## API Examples

### Record Impression Event
```bash
curl -X POST http://localhost:8085/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "id": "event-123",
    "eventType": "IMPRESSION",
    "adId": "ad-456",
    "campaignId": "campaign-789",
    "timestamp": "2026-01-22T12:00:00Z",
    "metadata": {
      "userAgent": "Mozilla/5.0",
      "country": "KR"
    }
  }'
```

### Record Click Event
```bash
curl -X POST http://localhost:8085/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "id": "event-456",
    "eventType": "CLICK",
    "adId": "ad-456",
    "campaignId": "campaign-789",
    "timestamp": "2026-01-22T12:01:00Z",
    "impressionToken": "impression-token-123"
  }'
```

### Get Events by Ad ID
```bash
curl http://localhost:8085/api/v1/events/ad/ad-456
```

### Get Events by Time Range
```bash
curl "http://localhost:8085/api/v1/events?startTime=2026-01-22T00:00:00Z&endTime=2026-01-22T23:59:59Z"
```

## Key Features

### 1. Append-Only Design
- Events are immutable after creation
- No update or delete operations
- Optimized for write-heavy workloads
- Audit trail preservation

### 2. Idempotent Recording
- Duplicate event IDs are handled gracefully
- Returns existing event on duplicate
- Prevents data duplication
- Safe for retry scenarios

### 3. Flexible Metadata
- Key-value store for additional data
- No schema restrictions
- Stored in separate table
- Indexed for efficient queries

### 4. Performance Optimizations
- Database indexes on: ad_id, timestamp, campaign_id
- @Immutable annotation for Hibernate
- Connection pooling (HikariCP)
- Timestamp-based queries optimized with index

### 5. Business Rule Enforcement
- Click/Conversion tracking via impressionToken
- Required field validation
- Timestamp validation (no future dates)
- Type-safe event types (enum)

## Project Structure

```
eventlog-module/
├── src/
│   ├── main/
│   │   ├── java/com/adplatform/eventlog/
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── AdEvent.java (Aggregate Root)
│   │   │   │   │   └── EventType.java (Enum)
│   │   │   │   └── repository/
│   │   │   │       └── EventRepository.java (Port)
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   │   ├── RecordEventCommand.java
│   │   │   │   │   └── EventResult.java
│   │   │   │   └── usecase/
│   │   │   │       ├── RecordEventUseCase.java
│   │   │   │       ├── GetEventsByAdUseCase.java
│   │   │   │       └── GetEventsByTimeRangeUseCase.java
│   │   │   ├── infrastructure/
│   │   │   │   └── persistence/
│   │   │   │       ├── AdEventEntity.java (JPA)
│   │   │   │       ├── AdEventJpaRepository.java
│   │   │   │       └── EventRepositoryImpl.java (Adapter)
│   │   │   ├── adapter/
│   │   │   │   └── rest/
│   │   │   │       ├── EventLogController.java
│   │   │   │       ├── GlobalExceptionHandler.java
│   │   │   │       └── dto/
│   │   │   │           ├── RecordEventRequest.java
│   │   │   │           └── EventResponse.java
│   │   │   └── EventLogServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── schema.sql
│   └── test/
│       ├── java/com/adplatform/eventlog/
│       │   ├── domain/model/
│       │   │   └── AdEventTest.java (19 tests)
│       │   ├── application/usecase/
│       │   │   ├── RecordEventUseCaseTest.java (4 tests)
│       │   │   ├── GetEventsByAdUseCaseTest.java (4 tests)
│       │   │   └── GetEventsByTimeRangeUseCaseTest.java (5 tests)
│       │   ├── infrastructure/persistence/
│       │   │   └── EventRepositoryImplTest.java (8 tests)
│       │   └── adapter/rest/
│       │       └── EventLogControllerTest.java (11 tests)
│       └── resources/
│           └── application-test.yml
├── build.gradle
└── Dockerfile
```

## Build and Run

### Build
```bash
./gradlew :eventlog-module:build
```

### Run Tests
```bash
./gradlew :eventlog-module:test
```

### Run Locally
```bash
./gradlew :eventlog-module:bootRun
```

### Docker
```bash
# Build all services
docker-compose build

# Start eventlog service
docker-compose up eventlog-service

# Or start all services
docker-compose up
```

## Design Patterns Applied

1. **Domain-Driven Design (DDD)**
   - Aggregate Root (AdEvent)
   - Repository Pattern (EventRepository)
   - Domain Events (implicit in event logging)
   - Ubiquitous Language

2. **Hexagonal Architecture (Ports & Adapters)**
   - Domain Layer: Pure business logic
   - Application Layer: Use cases (orchestration)
   - Infrastructure Layer: Technical implementations
   - Adapter Layer: External interfaces (REST)

3. **CQRS Concepts**
   - Write: RecordEventUseCase
   - Read: GetEventsByAdUseCase, GetEventsByTimeRangeUseCase
   - Optimized for different access patterns

4. **Immutability**
   - Events are value objects
   - No setters, only getters
   - Builder pattern for construction

5. **Idempotency**
   - Duplicate detection
   - Safe retry mechanisms

## Best Practices Implemented

✅ **Test-Driven Development (TDD)**: Tests written first, 51 tests covering all layers
✅ **Clean Architecture**: Clear separation of concerns across layers
✅ **SOLID Principles**: Single responsibility, dependency inversion
✅ **Immutability**: Events cannot be modified after creation
✅ **Validation**: Multiple layers (domain, application, API)
✅ **Error Handling**: Centralized exception handling
✅ **Logging**: Structured logging with SLF4J
✅ **Database Optimization**: Indexes on frequently queried fields
✅ **API Design**: RESTful, meaningful HTTP status codes
✅ **Configuration**: Externalized via Spring properties
✅ **Documentation**: Comprehensive inline comments

## Future Enhancements

1. **Event Streaming**: Integration with Kafka/RabbitMQ
2. **Analytics**: Real-time metrics and aggregations
3. **Partitioning**: Time-based table partitioning for scalability
4. **Caching**: Redis for frequently accessed events
5. **Async Processing**: Non-blocking event recording
6. **Event Sourcing**: Full event sourcing implementation
7. **GetById Use Case**: Complete the GET /api/v1/events/{id} endpoint

## Summary

The EventLog module is a **production-ready, fully tested implementation** that follows industry best practices:

- ✅ **51 comprehensive tests** covering all layers
- ✅ **100% test pass rate**
- ✅ **Clean DDD architecture** with clear boundaries
- ✅ **Immutable, append-only design** for audit integrity
- ✅ **Idempotent operations** for reliability
- ✅ **Performance optimized** with proper indexing
- ✅ **REST API** on port 8085
- ✅ **Docker ready** with docker-compose integration
- ✅ **Business rules enforced** in domain layer

The module is ready for deployment and integration with the rest of the ad platform.
