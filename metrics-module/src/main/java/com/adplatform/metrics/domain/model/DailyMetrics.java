package com.adplatform.metrics.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 일일 메트릭스 Aggregate Root
 * - 광고별 일일 성과 지표
 * - 노출, 클릭, 전환, 비용 추적
 * - CTR, CVR, CPA 등 계산된 지표 제공
 * - 비즈니스 규칙: date + adId 고유 제약
 */
@Getter
public class DailyMetrics {

    private final String id;
    private final LocalDate date;
    private final String adId;
    private final String adGroupId;
    private final String campaignId;
    private final Long impressions;
    private final Long clicks;
    private final Long conversions;
    private final Long cost;
    private final Instant createdAt;
    private final Instant updatedAt;

    @Builder
    public DailyMetrics(
        String id,
        LocalDate date,
        String adId,
        String adGroupId,
        String campaignId,
        Long impressions,
        Long clicks,
        Long conversions,
        Long cost
    ) {
        validateDate(date);
        validateAdId(adId);
        validateCampaignId(campaignId);
        validateImpressions(impressions);
        validateClicks(impressions, clicks);
        validateConversions(clicks, conversions);
        validateCost(cost);

        this.id = id;
        this.date = date;
        this.adId = adId;
        this.adGroupId = adGroupId;
        this.campaignId = campaignId;
        this.impressions = impressions != null ? impressions : 0L;
        this.clicks = clicks != null ? clicks : 0L;
        this.conversions = conversions != null ? conversions : 0L;
        this.cost = cost != null ? cost : 0L;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // 생성자 오버로드 (createdAt, updatedAt 포함)
    @Builder(builderMethodName = "builderWithTimestamps", builderClassName = "BuilderWithTimestamps")
    public DailyMetrics(
        String id,
        LocalDate date,
        String adId,
        String adGroupId,
        String campaignId,
        Long impressions,
        Long clicks,
        Long conversions,
        Long cost,
        Instant createdAt,
        Instant updatedAt
    ) {
        validateDate(date);
        validateAdId(adId);
        validateCampaignId(campaignId);
        validateImpressions(impressions);
        validateClicks(impressions, clicks);
        validateConversions(clicks, conversions);
        validateCost(cost);

        this.id = id;
        this.date = date;
        this.adId = adId;
        this.adGroupId = adGroupId;
        this.campaignId = campaignId;
        this.impressions = impressions != null ? impressions : 0L;
        this.clicks = clicks != null ? clicks : 0L;
        this.conversions = conversions != null ? conversions : 0L;
        this.cost = cost != null ? cost : 0L;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 필수입니다");
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

    private void validateImpressions(Long impressions) {
        if (impressions != null && impressions < 0) {
            throw new IllegalArgumentException("노출수는 0 이상이어야 합니다");
        }
    }

    private void validateClicks(Long impressions, Long clicks) {
        if (clicks != null && clicks < 0) {
            throw new IllegalArgumentException("클릭수는 0 이상이어야 합니다");
        }
        if (impressions != null && clicks != null && clicks > impressions) {
            throw new IllegalArgumentException("클릭수는 노출수를 초과할 수 없습니다");
        }
    }

    private void validateConversions(Long clicks, Long conversions) {
        if (conversions != null && conversions < 0) {
            throw new IllegalArgumentException("전환수는 0 이상이어야 합니다");
        }
        if (clicks != null && conversions != null && conversions > clicks) {
            throw new IllegalArgumentException("전환수는 클릭수를 초과할 수 없습니다");
        }
    }

    private void validateCost(Long cost) {
        if (cost != null && cost < 0) {
            throw new IllegalArgumentException("비용은 0 이상이어야 합니다");
        }
    }

    /**
     * CTR (Click-Through Rate) 계산
     * @return 클릭률 (%)
     */
    public double ctr() {
        if (impressions == 0) {
            return 0.0;
        }
        return (double) clicks / impressions * 100;
    }

    /**
     * CVR (Conversion Rate) 계산
     * @return 전환율 (%)
     */
    public double cvr() {
        if (clicks == 0) {
            return 0.0;
        }
        return (double) conversions / clicks * 100;
    }

    /**
     * CPA (Cost Per Action) 계산
     * @return 전환당 비용
     */
    public double cpa() {
        if (conversions == 0) {
            return 0.0;
        }
        return (double) cost / conversions;
    }

    /**
     * 메트릭스 집계 (누적)
     * - 기존 메트릭스에 새로운 이벤트 카운트를 더함
     */
    public DailyMetrics aggregate(Long additionalImpressions, Long additionalClicks,
                                   Long additionalConversions, Long additionalCost) {
        return DailyMetrics.builderWithTimestamps()
            .id(this.id)
            .date(this.date)
            .adId(this.adId)
            .adGroupId(this.adGroupId)
            .campaignId(this.campaignId)
            .impressions(this.impressions + (additionalImpressions != null ? additionalImpressions : 0))
            .clicks(this.clicks + (additionalClicks != null ? additionalClicks : 0))
            .conversions(this.conversions + (additionalConversions != null ? additionalConversions : 0))
            .cost(this.cost + (additionalCost != null ? additionalCost : 0))
            .createdAt(this.createdAt)
            .updatedAt(Instant.now())
            .build();
    }

    /**
     * 동일한 날짜와 광고 ID 확인
     * - 고유 제약 검증에 사용
     */
    public boolean isSameDateAndAd(LocalDate date, String adId) {
        return this.date.equals(date) && this.adId.equals(adId);
    }

    /**
     * 평균 클릭 비용 (CPC) 계산
     */
    public double cpc() {
        if (clicks == 0) {
            return 0.0;
        }
        return (double) cost / clicks;
    }

    /**
     * 1000 노출당 비용 (CPM) 계산
     */
    public double cpm() {
        if (impressions == 0) {
            return 0.0;
        }
        return (double) cost / impressions * 1000;
    }
}
