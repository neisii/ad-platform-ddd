package com.adplatform.eventlog.adapter.rest.dto;

import com.adplatform.eventlog.application.dto.EventResult;
import com.adplatform.eventlog.domain.model.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 이벤트 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private String id;
    private EventType eventType;
    private String adId;
    private String campaignId;
    private String adGroupId;
    private String userId;
    private String sessionId;
    private Instant timestamp;
    private Map<String, String> metadata;
    private String impressionToken;

    public static EventResponse from(EventResult result) {
        return EventResponse.builder()
            .id(result.getId())
            .eventType(result.getEventType())
            .adId(result.getAdId())
            .campaignId(result.getCampaignId())
            .adGroupId(result.getAdGroupId())
            .userId(result.getUserId())
            .sessionId(result.getSessionId())
            .timestamp(result.getTimestamp())
            .metadata(result.getMetadata())
            .impressionToken(result.getImpressionToken())
            .build();
    }
}
