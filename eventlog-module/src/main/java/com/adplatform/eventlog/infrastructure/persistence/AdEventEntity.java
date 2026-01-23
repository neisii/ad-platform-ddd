package com.adplatform.eventlog.infrastructure.persistence;

import com.adplatform.eventlog.domain.model.AdEvent;
import com.adplatform.eventlog.domain.model.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 광고 이벤트 JPA Entity
 * - @Immutable: 엔티티가 불변임을 명시 (Hibernate 최적화)
 * - Append-only: 생성 후 수정/삭제 불가
 */
@Entity
@Table(name = "ad_events", indexes = {
    @Index(name = "idx_ad_id", columnList = "ad_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_campaign_id", columnList = "campaign_id")
})
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdEventEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "event_type", updatable = false)
    private EventType eventType;

    @Column(nullable = false, name = "ad_id", updatable = false)
    private String adId;

    @Column(nullable = false, name = "campaign_id", updatable = false)
    private String campaignId;

    @Column(name = "ad_group_id", updatable = false)
    private String adGroupId;

    @Column(name = "user_id", updatable = false)
    private String userId;

    @Column(name = "session_id", updatable = false)
    private String sessionId;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ad_event_metadata", joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    private Map<String, String> metadata = new HashMap<>();

    @Column(name = "impression_token", updatable = false)
    private String impressionToken;

    /**
     * 도메인 모델로부터 Entity 생성
     */
    public static AdEventEntity from(AdEvent event) {
        return new AdEventEntity(
            event.getId(),
            event.getEventType(),
            event.getAdId(),
            event.getCampaignId(),
            event.getAdGroupId(),
            event.getUserId(),
            event.getSessionId(),
            event.getTimestamp(),
            new HashMap<>(event.getMetadata()),
            event.getImpressionToken()
        );
    }

    /**
     * Entity를 도메인 모델로 변환
     */
    public AdEvent toDomain() {
        return AdEvent.builder()
            .id(id)
            .eventType(eventType)
            .adId(adId)
            .campaignId(campaignId)
            .adGroupId(adGroupId)
            .userId(userId)
            .sessionId(sessionId)
            .timestamp(timestamp)
            .metadata(metadata)
            .impressionToken(impressionToken)
            .build();
    }
}
