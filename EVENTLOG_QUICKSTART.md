# EventLog Module - Quick Start Guide

## What is EventLog Module?
A production-ready service for tracking advertising events (impressions, clicks, conversions) with append-only, immutable event storage.

## Quick Stats
- **Port**: 8085
- **Database**: PostgreSQL (ad_platform_eventlog)
- **Files**: 22 Java files (16 production + 6 test)
- **Tests**: 51 comprehensive tests (100% passing)
- **Architecture**: DDD + Hexagonal + TDD

## Start the Service

### Option 1: Docker (Recommended)
```bash
# Start all services including eventlog
docker-compose up eventlog-service

# Or start with dependencies
docker-compose up postgres eventlog-service
```

### Option 2: Local Development
```bash
# Ensure PostgreSQL is running
# Database: ad_platform_eventlog

# Run the service
./gradlew :eventlog-module:bootRun

# Service starts at http://localhost:8085
```

## API Endpoints

### Base URL
```
http://localhost:8085/api/v1/events
```

### 1. Record Impression Event
```bash
POST /api/v1/events
Content-Type: application/json

{
  "id": "evt-impression-001",
  "eventType": "IMPRESSION",
  "adId": "ad-123",
  "campaignId": "camp-456",
  "timestamp": "2026-01-22T12:00:00Z",
  "userId": "user-789",
  "sessionId": "session-abc",
  "metadata": {
    "userAgent": "Mozilla/5.0",
    "country": "KR",
    "device": "mobile"
  }
}

Response: 202 Accepted
```

### 2. Record Click Event
```bash
POST /api/v1/events
Content-Type: application/json

{
  "id": "evt-click-001",
  "eventType": "CLICK",
  "adId": "ad-123",
  "campaignId": "camp-456",
  "timestamp": "2026-01-22T12:01:00Z",
  "impressionToken": "evt-impression-001",  # Links to impression
  "userId": "user-789",
  "sessionId": "session-abc"
}

Response: 202 Accepted
```

### 3. Record Conversion Event
```bash
POST /api/v1/events
Content-Type: application/json

{
  "id": "evt-conversion-001",
  "eventType": "CONVERSION",
  "adId": "ad-123",
  "campaignId": "camp-456",
  "timestamp": "2026-01-22T12:05:00Z",
  "impressionToken": "evt-impression-001",  # Links to impression
  "userId": "user-789",
  "metadata": {
    "conversionValue": "99.99",
    "currency": "USD"
  }
}

Response: 202 Accepted
```

### 4. Get Events by Ad ID
```bash
GET /api/v1/events/ad/{adId}

Example:
GET /api/v1/events/ad/ad-123

Response: 200 OK
[
  {
    "id": "evt-impression-001",
    "eventType": "IMPRESSION",
    "adId": "ad-123",
    "timestamp": "2026-01-22T12:00:00Z",
    ...
  },
  {
    "id": "evt-click-001",
    "eventType": "CLICK",
    "adId": "ad-123",
    "timestamp": "2026-01-22T12:01:00Z",
    ...
  }
]
```

### 5. Get Events by Time Range
```bash
GET /api/v1/events?startTime={iso8601}&endTime={iso8601}

Example:
GET /api/v1/events?startTime=2026-01-22T00:00:00Z&endTime=2026-01-22T23:59:59Z

Response: 200 OK
[
  { event1 },
  { event2 },
  ...
]
```

## Event Types

### IMPRESSION
- **Purpose**: Track when an ad is displayed
- **Required**: id, eventType, adId, campaignId, timestamp
- **Optional**: userId, sessionId, adGroupId, metadata
- **No impressionToken needed**

### CLICK
- **Purpose**: Track when a user clicks an ad
- **Required**: id, eventType, adId, campaignId, timestamp, **impressionToken**
- **impressionToken**: Must reference a previous IMPRESSION event

### CONVERSION
- **Purpose**: Track when a user completes desired action
- **Required**: id, eventType, adId, campaignId, timestamp, **impressionToken**
- **impressionToken**: Must reference a previous IMPRESSION event

## Business Rules

1. ✅ **Idempotent**: Duplicate event IDs return existing event (safe to retry)
2. ✅ **Immutable**: Events cannot be modified after creation
3. ✅ **Append-only**: No delete operations
4. ✅ **Timestamp validation**: Cannot be in future
5. ✅ **Click/Conversion tracking**: Must have impressionToken
6. ✅ **Flexible metadata**: Store any key-value data

## Testing

### Run All Tests
```bash
./gradlew :eventlog-module:test
```

### Test Coverage
- Domain Layer: 19 tests
- Application Layer: 13 tests
- Infrastructure Layer: 8 tests
- REST API Layer: 11 tests
- **Total: 51 tests - ALL PASSING ✅**

### Test a Specific Layer
```bash
# Domain tests
./gradlew :eventlog-module:test --tests "com.adplatform.eventlog.domain.*"

# Use case tests
./gradlew :eventlog-module:test --tests "com.adplatform.eventlog.application.usecase.*"

# API tests
./gradlew :eventlog-module:test --tests "com.adplatform.eventlog.adapter.rest.*"
```

## Database

### Connection
```yaml
URL: jdbc:postgresql://localhost:5432/ad_platform_eventlog
Username: postgres
Password: postgres
```

### Schema
Two tables:
1. `ad_events`: Main event storage
2. `ad_event_metadata`: Key-value metadata

### Indexes (for performance)
- `idx_ad_id`: Query events by ad
- `idx_timestamp`: Time-range queries
- `idx_campaign_id`: Campaign analytics

## cURL Examples

### Record an impression with metadata
```bash
curl -X POST http://localhost:8085/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-impression-001",
    "eventType": "IMPRESSION",
    "adId": "ad-test-123",
    "campaignId": "campaign-test-456",
    "timestamp": "2026-01-22T12:00:00Z",
    "metadata": {
      "userAgent": "curl/7.64.1",
      "testEvent": "true"
    }
  }'
```

### Record a click (linking to impression)
```bash
curl -X POST http://localhost:8085/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-click-001",
    "eventType": "CLICK",
    "adId": "ad-test-123",
    "campaignId": "campaign-test-456",
    "timestamp": "2026-01-22T12:00:30Z",
    "impressionToken": "test-impression-001"
  }'
```

### Query events for an ad
```bash
curl http://localhost:8085/api/v1/events/ad/ad-test-123
```

### Query events by time
```bash
curl "http://localhost:8085/api/v1/events?startTime=2026-01-22T00:00:00Z&endTime=2026-01-22T23:59:59Z"
```

## Error Handling

### 400 Bad Request
- Missing required fields
- Invalid event type
- CLICK/CONVERSION without impressionToken
- Invalid timestamp (future date)
- Invalid time range (start > end)

Example:
```json
{
  "timestamp": "2026-01-22T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "CLICK 이벤트는 impressionToken이 필수입니다"
}
```

### 500 Internal Server Error
- Database connection issues
- Unexpected server errors

## Monitoring

### Health Check
```bash
curl http://localhost:8085/actuator/health
```

### Logs
```bash
# View logs
docker-compose logs -f eventlog-service

# Or if running locally
tail -f eventlog-module/logs/application.log
```

## Common Use Cases

### 1. Track Ad Performance
```bash
# Record impression
POST /api/v1/events (IMPRESSION)

# Record click
POST /api/v1/events (CLICK with impressionToken)

# Query all events for the ad
GET /api/v1/events/ad/{adId}

# Calculate CTR = clicks / impressions
```

### 2. Conversion Tracking
```bash
# Record impression
POST /api/v1/events (IMPRESSION)

# User clicks
POST /api/v1/events (CLICK)

# User converts
POST /api/v1/events (CONVERSION with impressionToken)

# Calculate conversion rate
```

### 3. Time-based Analytics
```bash
# Get events for last hour
GET /api/v1/events?startTime=...&endTime=...

# Aggregate by event type
# Group by hour/day
```

## Architecture Layers

```
┌─────────────────────────────────────┐
│     REST API (Port 8085)            │  ← EventLogController
├─────────────────────────────────────┤
│     Application Layer               │  ← Use Cases
│  - RecordEventUseCase               │
│  - GetEventsByAdUseCase             │
│  - GetEventsByTimeRangeUseCase      │
├─────────────────────────────────────┤
│     Domain Layer                    │  ← Business Logic
│  - AdEvent (Aggregate Root)         │
│  - EventType (Enum)                 │
│  - EventRepository (Interface)      │
├─────────────────────────────────────┤
│     Infrastructure Layer            │  ← Technical Details
│  - AdEventEntity (JPA)              │
│  - EventRepositoryImpl              │
│  - PostgreSQL Database              │
└─────────────────────────────────────┘
```

## Key Features

✅ **Idempotent** - Safe to retry failed requests
✅ **Immutable** - Events never change
✅ **Append-only** - Perfect for audit trails
✅ **Performant** - Indexed for fast queries
✅ **Flexible** - Store custom metadata
✅ **Type-safe** - Strong typing with enums
✅ **Tested** - 51 comprehensive tests
✅ **RESTful** - Standard HTTP APIs

## Troubleshooting

### Service won't start
```bash
# Check database connection
psql -h localhost -U postgres -d ad_platform_eventlog

# Check port availability
lsof -i :8085

# Check logs
docker-compose logs eventlog-service
```

### Tests failing
```bash
# Clean and rebuild
./gradlew :eventlog-module:clean :eventlog-module:test

# Check H2 test database
# (Should be automatically created)
```

### Can't record events
```bash
# Check validation errors in response
# Ensure required fields are present
# Verify timestamp format (ISO-8601)
# For CLICK/CONVERSION: include impressionToken
```

## Next Steps

1. **Integration**: Connect inventory-service to record impressions
2. **Analytics**: Build real-time dashboards
3. **Streaming**: Add Kafka for event streaming
4. **Monitoring**: Set up Prometheus metrics
5. **Scaling**: Implement database partitioning

## Support

- Documentation: `/EVENTLOG_MODULE_IMPLEMENTATION.md`
- Tests: `/eventlog-module/src/test/java/`
- Source: `/eventlog-module/src/main/java/`

---

**Service is ready for production use! 🚀**
