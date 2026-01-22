-- PostgreSQL 초기화 스크립트

-- Campaign 모듈용 테이블은 JPA가 자동 생성하므로 여기서는 스키마만 준비

-- 필요시 추가 설정
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 각 모듈별 스키마 분리 (선택사항)
-- CREATE SCHEMA IF NOT EXISTS campaign;
-- CREATE SCHEMA IF NOT EXISTS advertiser;
-- CREATE SCHEMA IF NOT EXISTS targeting;
-- CREATE SCHEMA IF NOT EXISTS inventory;
-- CREATE SCHEMA IF NOT EXISTS eventlog;
-- CREATE SCHEMA IF NOT EXISTS metrics;
-- CREATE SCHEMA IF NOT EXISTS billing;

-- 인덱스는 JPA가 생성하지만, 성능 최적화를 위해 추가 인덱스 생성 가능
-- 예: CREATE INDEX idx_campaigns_advertiser ON campaigns(advertiser_id);
