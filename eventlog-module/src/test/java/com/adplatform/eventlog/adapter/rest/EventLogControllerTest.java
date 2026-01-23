package com.adplatform.eventlog.adapter.rest;

import com.adplatform.eventlog.adapter.rest.dto.RecordEventRequest;
import com.adplatform.eventlog.domain.model.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("EventLogController 통합 테스트")
class EventLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/events - IMPRESSION 이벤트 기록 성공")
    void recordImpressionEventSuccessfully() throws Exception {
        // given
        RecordEventRequest request = RecordEventRequest.builder()
            .id("event-test-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.id").value("event-test-123"))
            .andExpect(jsonPath("$.eventType").value("IMPRESSION"))
            .andExpect(jsonPath("$.adId").value("ad-456"))
            .andExpect(jsonPath("$.campaignId").value("campaign-789"));
    }

    @Test
    @DisplayName("POST /api/v1/events - CLICK 이벤트 기록 성공")
    void recordClickEventSuccessfully() throws Exception {
        // given
        RecordEventRequest request = RecordEventRequest.builder()
            .id("event-click-123")
            .eventType(EventType.CLICK)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .impressionToken("impression-token-123")
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.eventType").value("CLICK"))
            .andExpect(jsonPath("$.impressionToken").value("impression-token-123"));
    }

    @Test
    @DisplayName("POST /api/v1/events - 메타데이터 포함 이벤트 기록")
    void recordEventWithMetadata() throws Exception {
        // given
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userAgent", "Mozilla/5.0");
        metadata.put("country", "KR");

        RecordEventRequest request = RecordEventRequest.builder()
            .id("event-meta-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .metadata(metadata)
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.metadata.userAgent").value("Mozilla/5.0"))
            .andExpect(jsonPath("$.metadata.country").value("KR"));
    }

    @Test
    @DisplayName("POST /api/v1/events - 중복 이벤트 기록 시 Idempotent 처리")
    void recordDuplicateEventIsIdempotent() throws Exception {
        // given
        RecordEventRequest request = RecordEventRequest.builder()
            .id("event-duplicate-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();

        // 첫 번째 기록
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted());

        // when & then - 두 번째 기록 (중복)
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.id").value("event-duplicate-123"));
    }

    @Test
    @DisplayName("POST /api/v1/events - 필수 필드 누락 시 400 에러")
    void return400WhenRequiredFieldsMissing() throws Exception {
        // given - eventType이 없는 요청
        RecordEventRequest request = RecordEventRequest.builder()
            .id("event-invalid")
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/events - CLICK 이벤트에 impressionToken 없으면 400 에러")
    void return400WhenClickEventMissingImpressionToken() throws Exception {
        // given
        RecordEventRequest request = RecordEventRequest.builder()
            .id("event-click-invalid")
            .eventType(EventType.CLICK)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            // impressionToken 없음
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/events/ad/{adId} - 광고 ID로 이벤트 조회")
    void getEventsByAdId() throws Exception {
        // given - 먼저 이벤트 2개를 기록
        String adId = "ad-query-test";

        RecordEventRequest request1 = RecordEventRequest.builder()
            .id("event-query-1")
            .eventType(EventType.IMPRESSION)
            .adId(adId)
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();

        RecordEventRequest request2 = RecordEventRequest.builder()
            .id("event-query-2")
            .eventType(EventType.CLICK)
            .adId(adId)
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .impressionToken("token-123")
            .build();

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isAccepted());

        // when & then
        mockMvc.perform(get("/api/v1/events/ad/" + adId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].adId", everyItem(is(adId))));
    }

    @Test
    @DisplayName("GET /api/v1/events?startTime=...&endTime=... - 시간 범위로 이벤트 조회")
    void getEventsByTimeRange() throws Exception {
        // given
        Instant now = Instant.now();
        String startTime = now.minusSeconds(3600).toString();
        String endTime = now.plusSeconds(3600).toString();

        RecordEventRequest request = RecordEventRequest.builder()
            .id("event-time-query")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(now)
            .build();

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted());

        // when & then
        mockMvc.perform(get("/api/v1/events")
                .param("startTime", startTime)
                .param("endTime", endTime))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/events/ad/{adId} - 존재하지 않는 광고 ID로 조회 시 빈 배열")
    void returnEmptyArrayWhenNoEventsForAd() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/events/ad/non-existent-ad"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/events - 유효하지 않은 시간 범위로 조회 시 400 에러")
    void return400WhenInvalidTimeRange() throws Exception {
        // given - startTime이 endTime보다 이후
        Instant now = Instant.now();
        String startTime = now.toString();
        String endTime = now.minusSeconds(3600).toString();

        // when & then
        mockMvc.perform(get("/api/v1/events")
                .param("startTime", startTime)
                .param("endTime", endTime))
            .andExpect(status().isBadRequest());
    }
}
