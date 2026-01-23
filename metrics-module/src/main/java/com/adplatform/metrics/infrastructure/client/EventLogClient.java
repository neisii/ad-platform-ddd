package com.adplatform.metrics.infrastructure.client;

import com.adplatform.metrics.domain.service.MetricsCalculator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * EventLog 서비스 클라이언트
 * - 이벤트 조회를 위한 외부 서비스 통신
 */
@Component
@RequiredArgsConstructor
public class EventLogClient {

    private final RestTemplate restTemplate;
    private static final String EVENT_LOG_SERVICE_URL = "http://localhost:8084/api/v1/events";

    /**
     * 날짜 범위로 이벤트 조회
     */
    public List<AdEventDto> getEventsByDateRange(LocalDate startDate, LocalDate endDate) {
        String url = String.format("%s/range?startDate=%s&endDate=%s",
            EVENT_LOG_SERVICE_URL, startDate, endDate);

        AdEventDto[] events = restTemplate.getForObject(url, AdEventDto[].class);
        return events != null ? Arrays.asList(events) : List.of();
    }

    /**
     * 특정 날짜의 이벤트 조회
     */
    public List<AdEventDto> getEventsByDate(LocalDate date) {
        String url = String.format("%s/date?date=%s", EVENT_LOG_SERVICE_URL, date);

        AdEventDto[] events = restTemplate.getForObject(url, AdEventDto[].class);
        return events != null ? Arrays.asList(events) : List.of();
    }

    /**
     * 광고 ID와 날짜로 이벤트 조회
     */
    public List<AdEventDto> getEventsByAdIdAndDate(String adId, LocalDate date) {
        String url = String.format("%s/ad/%s?date=%s", EVENT_LOG_SERVICE_URL, adId, date);

        AdEventDto[] events = restTemplate.getForObject(url, AdEventDto[].class);
        return events != null ? Arrays.asList(events) : List.of();
    }

    /**
     * AdEvent DTO
     * - EventLog 서비스의 응답 DTO
     */
    @Getter
    public static class AdEventDto implements MetricsCalculator.EventData {
        private String id;
        private String eventType;
        private String adId;
        private String adGroupId;
        private String campaignId;
        private String userId;
        private String sessionId;
        private Instant timestamp;
        private String impressionToken;

        @Override
        public LocalDate getDate() {
            return timestamp.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }

        // Setters for Jackson deserialization
        public void setId(String id) {
            this.id = id;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public void setAdId(String adId) {
            this.adId = adId;
        }

        public void setAdGroupId(String adGroupId) {
            this.adGroupId = adGroupId;
        }

        public void setCampaignId(String campaignId) {
            this.campaignId = campaignId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public void setImpressionToken(String impressionToken) {
            this.impressionToken = impressionToken;
        }
    }
}
