# Metrics Module Quick Start Guide

## Overview
The Metrics module aggregates advertising events into daily performance metrics and provides analytics APIs.

**Port**: 8086  
**Database**: PostgreSQL (shared with other modules)

## Quick Start

### 1. Start Dependencies
```bash
# Start PostgreSQL
docker-compose up -d postgres

# Start EventLog service (required)
./gradlew :eventlog-module:bootRun

# Start Campaign service (required)
./gradlew :campaign-module:bootRun
```

### 2. Start Metrics Service
```bash
./gradlew :metrics-module:bootRun
```

The service will start on port 8086.

### 3. Verify Service is Running
```bash
curl http://localhost:8086/api/v1/metrics/health
```

Expected response:
```
Metrics Service is running
```

## Common Operations

### Trigger Manual Aggregation

Aggregate metrics for a specific date:
```bash
curl -X POST http://localhost:8086/api/v1/metrics/aggregate \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15"
  }'
```

Aggregate metrics for a date range:
```bash
curl -X POST http://localhost:8086/api/v1/metrics/aggregate \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2024-01-01",
    "endDate": "2024-01-15"
  }'
```

### Query Ad Metrics

Get metrics for a specific ad:
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
    "cpm": 50.0,
    "createdAt": "2024-01-15T10:00:00Z",
    "updatedAt": "2024-01-15T10:00:00Z"
  }
]
```

### Query Campaign Metrics

Get aggregated metrics for an entire campaign:
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
  "dailyMetrics": [
    {
      "id": "metrics-1",
      "date": "2024-01-15",
      "adId": "ad-123",
      "impressions": 10000,
      "clicks": 500,
      "conversions": 50,
      "cost": 500000,
      "ctr": 5.0,
      "cvr": 10.0,
      "cpa": 10000.0
    }
  ]
}
```

### Get Daily Metrics

Get all metrics for a specific date:
```bash
curl "http://localhost:8086/api/v1/metrics/daily?date=2024-01-15"
```

## Understanding the Metrics

### Basic Metrics
- **Impressions**: Number of times the ad was displayed
- **Clicks**: Number of times the ad was clicked
- **Conversions**: Number of desired actions completed
- **Cost**: Total cost incurred

### Calculated Metrics
- **CTR (Click-Through Rate)**: `(clicks / impressions) * 100`
  - Shows what percentage of impressions resulted in clicks
  
- **CVR (Conversion Rate)**: `(conversions / clicks) * 100`
  - Shows what percentage of clicks resulted in conversions
  
- **CPA (Cost Per Action)**: `cost / conversions`
  - Average cost per conversion
  
- **CPC (Cost Per Click)**: `cost / clicks`
  - Average cost per click
  
- **CPM (Cost Per Mille)**: `(cost / impressions) * 1000`
  - Cost per 1000 impressions

## Automatic Aggregation

The system automatically aggregates metrics on a schedule:

- **Hourly**: Every hour at minute 0 (cron: `0 0 * * * *`)
  - Aggregates yesterday's events
  
- **Daily**: Every day at midnight (cron: `0 0 0 * * *`)
  - Final aggregation of the previous day

You can monitor the logs to see when aggregation runs:
```bash
tail -f logs/metrics-service.log | grep "메트릭스 집계"
```

## Testing

### Run All Tests
```bash
./gradlew :metrics-module:test
```

### Run Specific Test Class
```bash
./gradlew :metrics-module:test --tests "DailyMetricsTest"
```

### Run with Coverage
```bash
./gradlew :metrics-module:test jacocoTestReport
```

## Complete Flow Example

Here's a complete example showing the entire flow from creating events to viewing metrics:

### 1. Create a Campaign (Campaign Service - port 8082)
```bash
curl -X POST http://localhost:8082/api/v1/campaigns \
  -H "Content-Type: application/json" \
  -d '{
    "advertiserId": "advertiser-1",
    "name": "Summer Sale Campaign",
    "dailyBudget": 100000,
    "totalBudget": 3000000,
    "startDate": "2024-01-01",
    "endDate": "2024-01-31"
  }'
```

### 2. Add Ad Group and Ads
```bash
# Add Ad Group
curl -X POST http://localhost:8082/api/v1/campaigns/{campaignId}/adgroups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product Ads",
    "bid": 1000
  }'

# Add Ad
curl -X POST http://localhost:8082/api/v1/campaigns/{campaignId}/adgroups/{adGroupId}/ads \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Banner Ad 1"
  }'
```

### 3. Track Events (EventLog Service - port 8084)
```bash
# Track Impression
curl -X POST http://localhost:8084/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "IMPRESSION",
    "adId": "ad-123",
    "adGroupId": "adgroup-123",
    "campaignId": "campaign-123",
    "userId": "user-1",
    "sessionId": "session-1"
  }'

# Track Click
curl -X POST http://localhost:8084/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "CLICK",
    "adId": "ad-123",
    "adGroupId": "adgroup-123",
    "campaignId": "campaign-123",
    "userId": "user-1",
    "sessionId": "session-1",
    "impressionToken": "{impressionToken}"
  }'
```

### 4. Aggregate Metrics (Metrics Service - port 8086)
```bash
curl -X POST http://localhost:8086/api/v1/metrics/aggregate \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-01-15"
  }'
```

### 5. Query Metrics
```bash
# Get ad metrics
curl "http://localhost:8086/api/v1/metrics/ad/ad-123?startDate=2024-01-01&endDate=2024-01-31"

# Get campaign metrics
curl "http://localhost:8086/api/v1/metrics/campaign/campaign-123?startDate=2024-01-01&endDate=2024-01-31"
```

## Troubleshooting

### Service won't start
**Issue**: Port 8086 already in use
```bash
# Find process using port 8086
lsof -i :8086

# Kill the process
kill -9 <PID>
```

### No metrics returned
**Checklist**:
1. ✅ EventLog service is running and has events
2. ✅ Campaign service is running and has campaign/ad data
3. ✅ Aggregation has been triggered (manually or via schedule)
4. ✅ Date range includes the aggregated dates
5. ✅ Database connection is working

### Aggregation fails
**Check logs**:
```bash
tail -f logs/metrics-service.log
```

Common issues:
- EventLog service not responding → Check if port 8084 is accessible
- Campaign service not responding → Check if port 8082 is accessible
- Database connection error → Check PostgreSQL is running
- No events found → Verify events exist in EventLog service

### Database schema issues
**Reset schema** (development only):
```bash
# Stop service
# Change application.yml: ddl-auto: create-drop
# Start service (will recreate tables)
# Change back to: ddl-auto: update
```

## Configuration

### Environment Variables
```bash
# Database
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ad_platform
export SPRING_DATASOURCE_USERNAME=adplatform
export SPRING_DATASOURCE_PASSWORD=adplatform123

# Server
export SERVER_PORT=8086

# External Services
export EVENTLOG_SERVICE_URL=http://localhost:8084
export CAMPAIGN_SERVICE_URL=http://localhost:8082
```

### application.yml
Located at: `metrics-module/src/main/resources/application.yml`

Key configurations:
- `server.port`: Service port (default: 8086)
- `spring.datasource.*`: Database connection
- `spring.jpa.hibernate.ddl-auto`: Schema management (update/create-drop)
- `logging.level.com.adplatform`: Log level (DEBUG/INFO)

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     Metrics Module (8086)                    │
├─────────────────────────────────────────────────────────────┤
│  REST API                                                    │
│  ├── POST /api/v1/metrics/aggregate                         │
│  ├── GET  /api/v1/metrics/ad/{adId}                         │
│  ├── GET  /api/v1/metrics/campaign/{campaignId}             │
│  └── GET  /api/v1/metrics/daily                             │
├─────────────────────────────────────────────────────────────┤
│  Use Cases                                                   │
│  ├── AggregateMetricsUseCase                                │
│  ├── GetMetricsByAdUseCase                                  │
│  └── GetMetricsByCampaignUseCase                            │
├─────────────────────────────────────────────────────────────┤
│  Domain                                                      │
│  ├── DailyMetrics (Aggregate Root)                          │
│  ├── MetricsCalculator (Domain Service)                     │
│  └── PricingModel (CPM/CPC/CPA)                             │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure                                              │
│  ├── DailyMetricsRepository (PostgreSQL)                    │
│  ├── EventLogClient (→ port 8084)                           │
│  └── CampaignClient (→ port 8082)                           │
└─────────────────────────────────────────────────────────────┘
```

## Key Features

✅ **Idempotent Aggregation**: Safe to run multiple times on same data  
✅ **Scheduled Jobs**: Automatic hourly and daily aggregation  
✅ **Calculated Metrics**: CTR, CVR, CPA, CPC, CPM  
✅ **Rollup Queries**: Campaign-level aggregation  
✅ **Multiple Pricing Models**: CPM, CPC, CPA support  
✅ **Date Range Queries**: Flexible time period filtering  

## Next Steps

1. Explore the API with Postman/Insomnia
2. Set up monitoring dashboards
3. Configure alerting for anomalies
4. Integrate with BI tools
5. Add custom metric calculations
6. Export data for analysis

For detailed implementation information, see [METRICS_MODULE_IMPLEMENTATION.md](./METRICS_MODULE_IMPLEMENTATION.md)
