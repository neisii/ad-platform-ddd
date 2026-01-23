# Metrics Module Implementation

## Overview
The Metrics module is responsible for aggregating advertising events into daily metrics and providing analytics capabilities. It follows Domain-Driven Design (DDD) principles and Test-Driven Development (TDD) practices.

## Architecture

### Domain Layer
- **DailyMetrics (Aggregate Root)**
  - Represents daily performance metrics for an advertisement
  - Fields: id, date, adId, adGroupId, campaignId, impressions, clicks, conversions, cost
  - Calculated metrics: CTR (Click-Through Rate), CVR (Conversion Rate), CPA (Cost Per Action), CPC, CPM
  - Business rule: Unique constraint on (date + adId)
  - Validation: Clicks cannot exceed impressions, conversions cannot exceed clicks

- **PricingModel (Enum)**
  - CPM: Cost Per Mille (1000 impressions)
  - CPC: Cost Per Click
  - CPA: Cost Per Action (Conversion)

- **MetricsCalculator (Domain Service)**
  - Aggregates events into DailyMetrics
  - Calculates cost based on pricing model and bid
  - Supports idempotent aggregation (upsert)

### Application Layer
- **Use Cases:**
  1. **AggregateMetricsUseCase**: Aggregates events into daily metrics (batch job)
  2. **GetMetricsByAdUseCase**: Retrieves metrics for a specific ad with date range
  3. **GetMetricsByCampaignUseCase**: Retrieves and rolls up campaign metrics

- **DTOs:**
  - CampaignMetricsDto: Aggregated campaign metrics with rollup calculations

### Infrastructure Layer
- **Repository:**
  - DailyMetricsRepository: Domain interface
  - DailyMetricsRepositoryImpl: JPA implementation with upsert logic
  - DailyMetricsJpaRepository: Spring Data JPA repository
  - DailyMetricsEntity: JPA entity with unique constraint
  - DailyMetricsMapper: Entity-Domain mapper

- **Clients:**
  - EventLogClient: Fetches events from EventLog service (port 8084)
  - CampaignClient: Fetches campaign/ad group information (port 8082)

### Adapter Layer
- **REST Controller (MetricsController):**
  - POST /api/v1/metrics/aggregate - Trigger aggregation (async)
  - GET /api/v1/metrics/ad/{adId}?startDate&endDate - Get ad metrics
  - GET /api/v1/metrics/campaign/{campaignId}?startDate&endDate - Get campaign metrics
  - GET /api/v1/metrics/daily?date={date} - Get all metrics for date
  - GET /api/v1/metrics/health - Health check

- **Scheduler:**
  - MetricsAggregationScheduler: Hourly aggregation job (cron: "0 0 * * * *")
  - Daily final aggregation at midnight (cron: "0 0 0 * * *")

## Key Features

### 1. Idempotent Aggregation
- Upsert operation based on (date + adId) unique constraint
- Safe to run multiple times on the same data
- Existing metrics are updated, not duplicated

### 2. Efficient Rollup Queries
- Campaign metrics aggregate all ads in a campaign
- Repository-level date range queries with JPA
- In-memory aggregation for rollup calculations

### 3. Calculated Metrics
- **CTR**: (clicks / impressions) * 100
- **CVR**: (conversions / clicks) * 100
- **CPA**: cost / conversions
- **CPC**: cost / clicks
- **CPM**: (cost / impressions) * 1000

### 4. Cost Calculation
- Based on pricing model (CPM, CPC, CPA)
- CPM: (bid * impressions) / 1000
- CPC: bid * clicks
- CPA: bid * conversions

### 5. Scheduled Aggregation
- Runs every hour to aggregate recent events
- Daily job at midnight for final aggregation
- Asynchronous processing to avoid blocking

## Testing

### Domain Tests
- ✅ DailyMetricsTest: 13 test cases
  - Creation, validation, calculated metrics
  - Business rule enforcement
  - Aggregation logic

- ✅ MetricsCalculatorTest: 7 test cases
  - Event aggregation
  - Cost calculation by pricing model
  - Zero metrics handling

### Use Case Tests
- ✅ AggregateMetricsUseCaseTest: 3 test cases
  - Date-based aggregation
  - Idempotent updates
  - Empty event handling

- ✅ GetMetricsByAdUseCaseTest: 2 test cases
  - Ad metrics retrieval
  - Empty results handling

- ✅ GetMetricsByCampaignUseCaseTest: 3 test cases
  - Campaign rollup
  - Calculated metrics accuracy
  - Zero metrics handling

### Integration Tests
- ✅ MetricsControllerTest: 4 test cases
  - REST API endpoints
  - Request/response validation
  - Health check

## Configuration

### Application Properties (port 8086)
```yaml
server:
  port: 8086

spring:
  application:
    name: metrics-service
  datasource:
    url: jdbc:postgresql://localhost:5432/ad_platform
  jpa:
    hibernate:
      ddl-auto: update
```

### External Service Dependencies
- EventLog Service: http://localhost:8084
- Campaign Service: http://localhost:8082

## Database Schema

### daily_metrics table
```sql
CREATE TABLE daily_metrics (
    id VARCHAR(255) PRIMARY KEY,
    date DATE NOT NULL,
    ad_id VARCHAR(255) NOT NULL,
    ad_group_id VARCHAR(255),
    campaign_id VARCHAR(255) NOT NULL,
    impressions BIGINT NOT NULL,
    clicks BIGINT NOT NULL,
    conversions BIGINT NOT NULL,
    cost BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_date_ad UNIQUE (date, ad_id)
);

CREATE INDEX idx_ad_date ON daily_metrics(ad_id, date);
CREATE INDEX idx_campaign_date ON daily_metrics(campaign_id, date);
CREATE INDEX idx_adgroup_date ON daily_metrics(ad_group_id, date);
CREATE INDEX idx_date ON daily_metrics(date);
```

## API Examples

### Trigger Aggregation
```bash
curl -X POST http://localhost:8086/api/v1/metrics/aggregate \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-01-15"}'
```

### Get Ad Metrics
```bash
curl "http://localhost:8086/api/v1/metrics/ad/ad-123?startDate=2024-01-01&endDate=2024-01-31"
```

Response:
```json
[
  {
    "id": "metrics-1",
    "date": "2024-01-15",
    "adId": "ad-123",
    "adGroupId": "adgroup-123",
    "campaignId": "campaign-123",
    "impressions": 10000,
    "clicks": 500,
    "conversions": 50,
    "cost": 500000,
    "ctr": 5.0,
    "cvr": 10.0,
    "cpa": 10000.0,
    "cpc": 1000.0,
    "cpm": 50.0
  }
]
```

### Get Campaign Metrics
```bash
curl "http://localhost:8086/api/v1/metrics/campaign/campaign-123?startDate=2024-01-01&endDate=2024-01-31"
```

Response:
```json
{
  "campaignId": "campaign-123",
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "totalImpressions": 100000,
  "totalClicks": 5000,
  "totalConversions": 500,
  "totalCost": 5000000,
  "ctr": 5.0,
  "cvr": 10.0,
  "cpa": 10000.0,
  "dailyMetrics": [...]
}
```

### Get Daily Metrics
```bash
curl "http://localhost:8086/api/v1/metrics/daily?date=2024-01-15"
```

## Running the Service

### Build
```bash
./gradlew :metrics-module:build
```

### Run Tests
```bash
./gradlew :metrics-module:test
```

### Start Service
```bash
./gradlew :metrics-module:bootRun
```

Or with Docker:
```bash
docker-compose up metrics-service
```

## Design Patterns Used

1. **Repository Pattern**: Separates domain logic from data access
2. **Mapper Pattern**: Converts between Entity and Domain models
3. **Builder Pattern**: Constructs complex DailyMetrics objects
4. **Strategy Pattern**: Different cost calculation strategies per pricing model
5. **Aggregate Root**: DailyMetrics controls its own invariants
6. **Value Object**: PricingModel as immutable enum

## Business Rules Enforced

1. ✅ Date + AdId unique constraint (no duplicates)
2. ✅ Clicks cannot exceed impressions
3. ✅ Conversions cannot exceed clicks
4. ✅ All counts must be non-negative
5. ✅ Required fields validation (date, adId, campaignId)
6. ✅ Idempotent aggregation (safe reruns)

## Test Coverage

- **Total Tests**: 32
- **Domain Layer**: 20 tests
- **Application Layer**: 8 tests
- **Integration Layer**: 4 tests
- **Success Rate**: 100% ✅

## Performance Considerations

1. **Batch Processing**: Events aggregated in batches, not one-by-one
2. **Indexed Queries**: Database indexes on frequently queried fields
3. **Rollup Calculations**: Done in-memory after fetching data
4. **Async Aggregation**: Non-blocking aggregation via threads
5. **Scheduled Jobs**: Hourly processing to distribute load

## Next Steps / Enhancements

1. Add caching layer (Redis) for frequently accessed metrics
2. Implement async processing with message queue (Kafka/RabbitMQ)
3. Add more granular metrics (hourly aggregation)
4. Implement metric comparisons (vs previous period)
5. Add alerting for anomaly detection
6. Create data warehouse export functionality
7. Add GraphQL API for flexible querying
8. Implement metric forecasting/predictions

## Dependencies

- Spring Boot 3.2.1
- Spring Data JPA
- PostgreSQL (runtime)
- H2 (test)
- Lombok
- JUnit 5
- Mockito
- AssertJ

## Module Status

✅ **COMPLETE** - All features implemented and tested

- ✅ Domain model with business rules
- ✅ Domain service with cost calculation
- ✅ Repository with upsert logic
- ✅ External service clients
- ✅ All use cases with tests
- ✅ REST API endpoints
- ✅ Scheduled aggregation job
- ✅ Spring Boot configuration
- ✅ Integration tests
- ✅ Build and deployment ready
