package com.adplatform.eventlog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AdEvent 도메인 모델 테스트")
class AdEventTest {

    @Test
    @DisplayName("유효한 IMPRESSION 이벤트 생성 성공")
    void createValidImpressionEvent() {
        // given
        String id = "event-123";
        String adId = "ad-456";
        String campaignId = "campaign-789";
        Instant timestamp = Instant.now();

        // when
        AdEvent event = AdEvent.builder()
            .id(id)
            .eventType(EventType.IMPRESSION)
            .adId(adId)
            .campaignId(campaignId)
            .timestamp(timestamp)
            .build();

        // then
        assertThat(event).isNotNull();
        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getEventType()).isEqualTo(EventType.IMPRESSION);
        assertThat(event.getAdId()).isEqualTo(adId);
        assertThat(event.getCampaignId()).isEqualTo(campaignId);
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.isImpression()).isTrue();
        assertThat(event.isClick()).isFalse();
        assertThat(event.isConversion()).isFalse();
    }

    @Test
    @DisplayName("유효한 CLICK 이벤트 생성 성공 - impressionToken 필요")
    void createValidClickEvent() {
        // given
        String impressionToken = "impression-token-123";

        // when
        AdEvent event = AdEvent.builder()
            .id("event-123")
            .eventType(EventType.CLICK)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .impressionToken(impressionToken)
            .build();

        // then
        assertThat(event).isNotNull();
        assertThat(event.getEventType()).isEqualTo(EventType.CLICK);
        assertThat(event.getImpressionToken()).isEqualTo(impressionToken);
        assertThat(event.isClick()).isTrue();
    }

    @Test
    @DisplayName("유효한 CONVERSION 이벤트 생성 성공 - impressionToken 필요")
    void createValidConversionEvent() {
        // given
        String impressionToken = "impression-token-456";

        // when
        AdEvent event = AdEvent.builder()
            .id("event-123")
            .eventType(EventType.CONVERSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .impressionToken(impressionToken)
            .build();

        // then
        assertThat(event).isNotNull();
        assertThat(event.getEventType()).isEqualTo(EventType.CONVERSION);
        assertThat(event.getImpressionToken()).isEqualTo(impressionToken);
        assertThat(event.isConversion()).isTrue();
    }

    @Test
    @DisplayName("메타데이터 포함 이벤트 생성 성공")
    void createEventWithMetadata() {
        // given
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userAgent", "Mozilla/5.0");
        metadata.put("ipAddress", "192.168.1.1");
        metadata.put("deviceType", "mobile");

        // when
        AdEvent event = AdEvent.builder()
            .id("event-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .metadata(metadata)
            .build();

        // then
        assertThat(event.getMetadata()).hasSize(3);
        assertThat(event.getMetadataValue("userAgent")).isEqualTo("Mozilla/5.0");
        assertThat(event.hasMetadata("deviceType")).isTrue();
        assertThat(event.hasMetadata("nonExistent")).isFalse();
    }

    @Test
    @DisplayName("메타데이터는 불변이어야 함")
    void metadataShouldBeImmutable() {
        // given
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "value");

        AdEvent event = AdEvent.builder()
            .id("event-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .metadata(metadata)
            .build();

        // when & then
        assertThatThrownBy(() -> event.getMetadata().put("newKey", "newValue"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("eventType이 null이면 예외 발생")
    void throwExceptionWhenEventTypeIsNull() {
        // when & then
        assertThatThrownBy(() -> AdEvent.builder()
            .id("event-123")
            .eventType(null)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이벤트 타입은 필수입니다");
    }

    @Test
    @DisplayName("adId가 null이면 예외 발생")
    void throwExceptionWhenAdIdIsNull() {
        // when & then
        assertThatThrownBy(() -> AdEvent.builder()
            .id("event-123")
            .eventType(EventType.IMPRESSION)
            .adId(null)
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("광고 ID는 필수입니다");
    }

    @Test
    @DisplayName("adId가 빈 문자열이면 예외 발생")
    void throwExceptionWhenAdIdIsEmpty() {
        // when & then
        assertThatThrownBy(() -> AdEvent.builder()
            .id("event-123")
            .eventType(EventType.IMPRESSION)
            .adId("   ")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("광고 ID는 필수입니다");
    }

    @Test
    @DisplayName("campaignId가 null이면 예외 발생")
    void throwExceptionWhenCampaignIdIsNull() {
        // when & then
        assertThatThrownBy(() -> AdEvent.builder()
            .id("event-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId(null)
            .timestamp(Instant.now())
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("캠페인 ID는 필수입니다");
    }

    @Test
    @DisplayName("timestamp가 null이면 예외 발생")
    void throwExceptionWhenTimestampIsNull() {
        // when & then
        assertThatThrownBy(() -> AdEvent.builder()
            .id("event-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(null)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("타임스탬프는 필수입니다");
    }

    @Test
    @DisplayName("timestamp가 미래 시간이면 예외 발생")
    void throwExceptionWhenTimestampIsInFuture() {
        // given
        Instant futureTime = Instant.now().plusSeconds(120);

        // when & then
        assertThatThrownBy(() -> AdEvent.builder()
            .id("event-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(futureTime)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("타임스탬프는 미래 시간일 수 없습니다");
    }

    @Test
    @DisplayName("CLICK 이벤트에 impressionToken이 없으면 예외 발생")
    void throwExceptionWhenClickEventHasNoImpressionToken() {
        // when & then
        assertThatThrownBy(() -> AdEvent.builder()
            .id("event-123")
            .eventType(EventType.CLICK)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CLICK 이벤트는 impressionToken이 필수입니다");
    }

    @Test
    @DisplayName("CONVERSION 이벤트에 impressionToken이 없으면 예외 발생")
    void throwExceptionWhenConversionEventHasNoImpressionToken() {
        // when & then
        assertThatThrownBy(() -> AdEvent.builder()
            .id("event-123")
            .eventType(EventType.CONVERSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("CONVERSION 이벤트는 impressionToken이 필수입니다");
    }

    @Test
    @DisplayName("IMPRESSION 이벤트는 impressionToken이 없어도 됨")
    void impressionEventDoesNotRequireImpressionToken() {
        // when
        AdEvent event = AdEvent.builder()
            .id("event-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();

        // then
        assertThat(event).isNotNull();
        assertThat(event.getImpressionToken()).isNull();
    }

    @Test
    @DisplayName("모든 필드를 포함한 완전한 이벤트 생성")
    void createCompleteEvent() {
        // given
        Map<String, String> metadata = new HashMap<>();
        metadata.put("country", "KR");

        // when
        AdEvent event = AdEvent.builder()
            .id("event-123")
            .eventType(EventType.CLICK)
            .adId("ad-456")
            .campaignId("campaign-789")
            .adGroupId("adgroup-101")
            .userId("user-202")
            .sessionId("session-303")
            .timestamp(Instant.now())
            .metadata(metadata)
            .impressionToken("impression-token-404")
            .build();

        // then
        assertThat(event.getId()).isEqualTo("event-123");
        assertThat(event.getEventType()).isEqualTo(EventType.CLICK);
        assertThat(event.getAdId()).isEqualTo("ad-456");
        assertThat(event.getCampaignId()).isEqualTo("campaign-789");
        assertThat(event.getAdGroupId()).isEqualTo("adgroup-101");
        assertThat(event.getUserId()).isEqualTo("user-202");
        assertThat(event.getSessionId()).isEqualTo("session-303");
        assertThat(event.getImpressionToken()).isEqualTo("impression-token-404");
        assertThat(event.getMetadata()).containsEntry("country", "KR");
    }
}
