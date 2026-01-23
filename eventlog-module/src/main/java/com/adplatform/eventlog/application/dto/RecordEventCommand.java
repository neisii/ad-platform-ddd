package com.adplatform.eventlog.application.dto;

import com.adplatform.eventlog.domain.model.EventType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * 이벤트 기록 커맨드
 */
@Getter
@Builder
public class RecordEventCommand {
    private final String id;
    private final EventType eventType;
    private final String adId;
    private final String campaignId;
    private final String adGroupId;
    private final String userId;
    private final String sessionId;
    private final Instant timestamp;
    private final Map<String, String> metadata;
    private final String impressionToken;
}
