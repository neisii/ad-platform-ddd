package com.adplatform.eventlog.adapter.rest;

import com.adplatform.eventlog.adapter.rest.dto.EventResponse;
import com.adplatform.eventlog.adapter.rest.dto.RecordEventRequest;
import com.adplatform.eventlog.application.dto.EventResult;
import com.adplatform.eventlog.application.dto.RecordEventCommand;
import com.adplatform.eventlog.application.usecase.GetEventsByAdUseCase;
import com.adplatform.eventlog.application.usecase.GetEventsByTimeRangeUseCase;
import com.adplatform.eventlog.application.usecase.RecordEventUseCase;
import com.adplatform.eventlog.domain.model.AdEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EventLog REST API Controller
 * - 광고 이벤트 기록 및 조회 API
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventLogController {

    private final RecordEventUseCase recordEventUseCase;
    private final GetEventsByAdUseCase getEventsByAdUseCase;
    private final GetEventsByTimeRangeUseCase getEventsByTimeRangeUseCase;

    /**
     * POST /api/v1/events - 이벤트 기록
     * @return 202 Accepted
     */
    @PostMapping
    public ResponseEntity<EventResponse> recordEvent(@Valid @RequestBody RecordEventRequest request) {
        log.info("Recording event: id={}, type={}, adId={}",
            request.getId(), request.getEventType(), request.getAdId());

        RecordEventCommand command = RecordEventCommand.builder()
            .id(request.getId())
            .eventType(request.getEventType())
            .adId(request.getAdId())
            .campaignId(request.getCampaignId())
            .adGroupId(request.getAdGroupId())
            .userId(request.getUserId())
            .sessionId(request.getSessionId())
            .timestamp(request.getTimestamp())
            .metadata(request.getMetadata())
            .impressionToken(request.getImpressionToken())
            .build();

        AdEvent event = recordEventUseCase.execute(command);
        EventResponse response = EventResponse.from(EventResult.from(event));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * GET /api/v1/events/{id} - 이벤트 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable String id) {
        log.info("Getting event: id={}", id);

        // Note: 이 기능을 위해서는 EventRepository에 findById를 노출하거나
        // 별도의 UseCase를 만들어야 합니다. 여기서는 간단히 구현합니다.
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * GET /api/v1/events/ad/{adId} - 광고 ID로 이벤트 조회
     */
    @GetMapping("/ad/{adId}")
    public ResponseEntity<List<EventResponse>> getEventsByAd(@PathVariable String adId) {
        log.info("Getting events by adId: {}", adId);

        List<AdEvent> events = getEventsByAdUseCase.execute(adId);
        List<EventResponse> responses = events.stream()
            .map(EventResult::from)
            .map(EventResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/v1/events?startTime={ts}&endTime={ts} - 시간 범위로 이벤트 조회
     */
    @GetMapping
    public ResponseEntity<List<EventResponse>> getEventsByTimeRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime
    ) {
        log.info("Getting events by time range: {} to {}", startTime, endTime);

        List<AdEvent> events = getEventsByTimeRangeUseCase.execute(startTime, endTime);
        List<EventResponse> responses = events.stream()
            .map(EventResult::from)
            .map(EventResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
