package com.adplatform.eventlog.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 광고 이벤트 Aggregate Root
 * - 광고 노출, 클릭, 전환 이벤트를 추적
 * - Immutable (불변 객체, append-only)
 * - 이벤트는 생성 후 수정/삭제 불가
 */
@Getter
public class AdEvent {

    private final String id;
    private final EventType eventType;
    private final String adId;
    private final String campaignId;
    private final String adGroupId;
    private final String userId;
    private final String sessionId;
    private final Instant timestamp;
    private final Map<String, String> metadata;
    private final String impressionToken;  // CLICK/CONVERSION 추적용

    @Builder
    public AdEvent(
        String id,
        EventType eventType,
        String adId,
        String campaignId,
        String adGroupId,
        String userId,
        String sessionId,
        Instant timestamp,
        Map<String, String> metadata,
        String impressionToken
    ) {
        // 비즈니스 규칙 검증
        validateEventType(eventType);
        validateAdId(adId);
        validateCampaignId(campaignId);
        validateTimestamp(timestamp);
        validateImpressionToken(eventType, impressionToken);

        this.id = id;
        this.eventType = eventType;
        this.adId = adId;
        this.campaignId = campaignId;
        this.adGroupId = adGroupId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.metadata = metadata != null
            ? Collections.unmodifiableMap(new HashMap<>(metadata))
            : Collections.emptyMap();
        this.impressionToken = impressionToken;
    }

    private void validateEventType(EventType eventType) {
        if (eventType == null) {
            throw new IllegalArgumentException("이벤트 타입은 필수입니다");
        }
    }

    private void validateAdId(String adId) {
        if (adId == null || adId.trim().isEmpty()) {
            throw new IllegalArgumentException("광고 ID는 필수입니다");
        }
    }

    private void validateCampaignId(String campaignId) {
        if (campaignId == null || campaignId.trim().isEmpty()) {
            throw new IllegalArgumentException("캠페인 ID는 필수입니다");
        }
    }

    private void validateTimestamp(Instant timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("타임스탬프는 필수입니다");
        }

        // 미래 시간 방지
        if (timestamp.isAfter(Instant.now().plusSeconds(60))) {
            throw new IllegalArgumentException("타임스탬프는 미래 시간일 수 없습니다");
        }
    }

    private void validateImpressionToken(EventType eventType, String impressionToken) {
        // CLICK과 CONVERSION은 impressionToken이 필수
        if ((eventType == EventType.CLICK || eventType == EventType.CONVERSION)
            && (impressionToken == null || impressionToken.trim().isEmpty())) {
            throw new IllegalArgumentException(
                eventType + " 이벤트는 impressionToken이 필수입니다");
        }
    }

    /**
     * IMPRESSION 이벤트인지 확인
     */
    public boolean isImpression() {
        return eventType == EventType.IMPRESSION;
    }

    /**
     * CLICK 이벤트인지 확인
     */
    public boolean isClick() {
        return eventType == EventType.CLICK;
    }

    /**
     * CONVERSION 이벤트인지 확인
     */
    public boolean isConversion() {
        return eventType == EventType.CONVERSION;
    }

    /**
     * 특정 메타데이터 값 조회
     */
    public String getMetadataValue(String key) {
        return metadata.get(key);
    }

    /**
     * 메타데이터 존재 여부 확인
     */
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
}
