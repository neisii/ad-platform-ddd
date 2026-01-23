-- EventLog Service Database Schema
-- Events are append-only (no updates or deletes)

CREATE TABLE IF NOT EXISTS ad_events (
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

CREATE TABLE IF NOT EXISTS ad_event_metadata (
    event_id VARCHAR(255) NOT NULL,
    meta_key VARCHAR(255) NOT NULL,
    meta_value TEXT,
    PRIMARY KEY (event_id, meta_key),
    FOREIGN KEY (event_id) REFERENCES ad_events(id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_ad_id ON ad_events(ad_id);
CREATE INDEX IF NOT EXISTS idx_timestamp ON ad_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_campaign_id ON ad_events(campaign_id);
CREATE INDEX IF NOT EXISTS idx_event_type ON ad_events(event_type);
CREATE INDEX IF NOT EXISTS idx_impression_token ON ad_events(impression_token);

-- Comments
COMMENT ON TABLE ad_events IS 'Append-only event log table for ad impressions, clicks, and conversions';
COMMENT ON COLUMN ad_events.timestamp IS 'Event timestamp, indexed for time-range queries';
COMMENT ON COLUMN ad_events.impression_token IS 'Used to track clicks and conversions back to impressions';
