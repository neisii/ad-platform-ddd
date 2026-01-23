package com.adplatform.eventlog.application.dto;

import com.adplatform.eventlog.domain.model.AdEvent;
import com.adplatform.eventlog.domain.model.EventType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * 이벤트 결과 DTO
 */
@Getter
@Builder
public class EventResult {
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

    public static EventResult from(AdEvent event) {
        return EventResult.builder()
            .id(event.getId())
            .eventType(event.getEventType())
            .adId(event.getAdId())
            .campaignId(event.getCampaignId())
            .adGroupId(event.getAdGroupId())
            .userId(event.getUserId())
            .sessionId(event.getSessionId())
            .timestamp(event.getTimestamp())
            .metadata(event.getMetadata())
            .impressionToken(event.getImpressionToken())
            .build();
    }
}
