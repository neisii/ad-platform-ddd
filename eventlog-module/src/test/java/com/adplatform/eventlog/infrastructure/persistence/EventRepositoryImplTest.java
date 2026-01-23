package com.adplatform.eventlog.infrastructure.persistence;

import com.adplatform.eventlog.domain.model.AdEvent;
import com.adplatform.eventlog.domain.model.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(EventRepositoryImpl.class)
@ActiveProfiles("test")
@DisplayName("EventRepositoryImpl 통합 테스트")
class EventRepositoryImplTest {

    @Autowired
    private EventRepositoryImpl eventRepository;

    @Test
    @DisplayName("이벤트 저장 및 조회 성공")
    void saveAndFindEvent() {
        // given
        AdEvent event = AdEvent.builder()
            .id("event-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();

        // when
        AdEvent saved = eventRepository.save(event);
        Optional<AdEvent> found = eventRepository.findById("event-123");

        // then
        assertThat(saved).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("event-123");
        assertThat(found.get().getEventType()).isEqualTo(EventType.IMPRESSION);
        assertThat(found.get().getAdId()).isEqualTo("ad-456");
    }

    @Test
    @DisplayName("메타데이터를 포함한 이벤트 저장 및 조회")
    void saveAndFindEventWithMetadata() {
        // given
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userAgent", "Mozilla/5.0");
        metadata.put("ipAddress", "192.168.1.1");

        AdEvent event = AdEvent.builder()
            .id("event-meta-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .metadata(metadata)
            .build();

        // when
        eventRepository.save(event);
        Optional<AdEvent> found = eventRepository.findById("event-meta-123");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getMetadata()).hasSize(2);
        assertThat(found.get().getMetadataValue("userAgent")).isEqualTo("Mozilla/5.0");
        assertThat(found.get().getMetadataValue("ipAddress")).isEqualTo("192.168.1.1");
    }

    @Test
    @DisplayName("광고 ID로 이벤트 목록 조회")
    void findEventsByAdId() {
        // given
        String adId = "ad-find-test";

        AdEvent event1 = AdEvent.builder()
            .id("event-1")
            .eventType(EventType.IMPRESSION)
            .adId(adId)
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();

        AdEvent event2 = AdEvent.builder()
            .id("event-2")
            .eventType(EventType.CLICK)
            .adId(adId)
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .impressionToken("token-123")
            .build();

        eventRepository.save(event1);
        eventRepository.save(event2);

        // when
        List<AdEvent> events = eventRepository.findByAdId(adId);

        // then
        assertThat(events).hasSize(2);
        assertThat(events).extracting(AdEvent::getAdId)
            .containsOnly(adId);
    }

    @Test
    @DisplayName("시간 범위로 이벤트 목록 조회")
    void findEventsByTimeRange() {
        // given
        Instant now = Instant.now();
        Instant startTime = now.minus(1, ChronoUnit.HOURS);
        Instant endTime = now.plus(1, ChronoUnit.HOURS);

        AdEvent event1 = AdEvent.builder()
            .id("event-time-1")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(now.minus(30, ChronoUnit.MINUTES))
            .build();

        AdEvent event2 = AdEvent.builder()
            .id("event-time-2")
            .eventType(EventType.IMPRESSION)
            .adId("ad-789")
            .campaignId("campaign-789")
            .timestamp(now.minus(10, ChronoUnit.MINUTES))
            .build();

        // 범위 밖의 이벤트
        AdEvent eventOutside = AdEvent.builder()
            .id("event-time-outside")
            .eventType(EventType.IMPRESSION)
            .adId("ad-outside")
            .campaignId("campaign-789")
            .timestamp(now.minus(2, ChronoUnit.HOURS))
            .build();

        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(eventOutside);

        // when
        List<AdEvent> events = eventRepository.findByTimeRange(startTime, endTime);

        // then
        assertThat(events).hasSize(2);
        assertThat(events).extracting(AdEvent::getId)
            .containsExactlyInAnyOrder("event-time-1", "event-time-2");
    }

    @Test
    @DisplayName("존재 여부 확인")
    void checkExistenceById() {
        // given
        AdEvent event = AdEvent.builder()
            .id("event-exists")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();

        eventRepository.save(event);

        // when & then
        assertThat(eventRepository.existsById("event-exists")).isTrue();
        assertThat(eventRepository.existsById("non-existent")).isFalse();
    }

    @Test
    @DisplayName("CLICK 이벤트 저장 및 조회 - impressionToken 포함")
    void saveAndFindClickEvent() {
        // given
        AdEvent clickEvent = AdEvent.builder()
            .id("event-click")
            .eventType(EventType.CLICK)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .impressionToken("impression-token-123")
            .build();

        // when
        eventRepository.save(clickEvent);
        Optional<AdEvent> found = eventRepository.findById("event-click");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEventType()).isEqualTo(EventType.CLICK);
        assertThat(found.get().getImpressionToken()).isEqualTo("impression-token-123");
    }

    @Test
    @DisplayName("모든 필드를 포함한 완전한 이벤트 저장 및 조회")
    void saveAndFindCompleteEvent() {
        // given
        Map<String, String> metadata = new HashMap<>();
        metadata.put("country", "KR");
        metadata.put("device", "mobile");

        AdEvent event = AdEvent.builder()
            .id("event-complete")
            .eventType(EventType.CONVERSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .adGroupId("adgroup-101")
            .userId("user-202")
            .sessionId("session-303")
            .timestamp(Instant.now())
            .metadata(metadata)
            .impressionToken("impression-token-404")
            .build();

        // when
        eventRepository.save(event);
        Optional<AdEvent> found = eventRepository.findById("event-complete");

        // then
        assertThat(found).isPresent();
        AdEvent foundEvent = found.get();
        assertThat(foundEvent.getId()).isEqualTo("event-complete");
        assertThat(foundEvent.getEventType()).isEqualTo(EventType.CONVERSION);
        assertThat(foundEvent.getAdId()).isEqualTo("ad-456");
        assertThat(foundEvent.getCampaignId()).isEqualTo("campaign-789");
        assertThat(foundEvent.getAdGroupId()).isEqualTo("adgroup-101");
        assertThat(foundEvent.getUserId()).isEqualTo("user-202");
        assertThat(foundEvent.getSessionId()).isEqualTo("session-303");
        assertThat(foundEvent.getImpressionToken()).isEqualTo("impression-token-404");
        assertThat(foundEvent.getMetadata()).hasSize(2);
    }
}
