package com.adplatform.metrics.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DailyMetrics JPA Entity
 * - 도메인 모델과 분리된 영속성 모델
 */
@Entity
@Table(
    name = "daily_metrics",
    uniqueConstraints = @UniqueConstraint(columnNames = {"date", "ad_id"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMetricsEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, name = "ad_id")
    private String adId;

    @Column(name = "ad_group_id")
    private String adGroupId;

    @Column(nullable = false, name = "campaign_id")
    private String campaignId;

    @Column(nullable = false)
    private Long impressions;

    @Column(nullable = false)
    private Long clicks;

    @Column(nullable = false)
    private Long conversions;

    @Column(nullable = false)
    private Long cost;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * 도메인 모델 변경사항 동기화
     */
    public void updateFromDomain(LocalDate date, String adId, String adGroupId,
                                  String campaignId, Long impressions, Long clicks,
                                  Long conversions, Long cost) {
        this.date = date;
        this.adId = adId;
        this.adGroupId = adGroupId;
        this.campaignId = campaignId;
        this.impressions = impressions;
        this.clicks = clicks;
        this.conversions = conversions;
        this.cost = cost;
    }
}
