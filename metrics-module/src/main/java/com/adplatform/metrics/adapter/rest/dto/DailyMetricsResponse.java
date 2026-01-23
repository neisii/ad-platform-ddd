package com.adplatform.metrics.adapter.rest.dto;

import com.adplatform.metrics.domain.model.DailyMetrics;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 일일 메트릭스 응답 DTO
 */
@Getter
@Builder
public class DailyMetricsResponse {
    private String id;
    private LocalDate date;
    private String adId;
    private String adGroupId;
    private String campaignId;
    private Long impressions;
    private Long clicks;
    private Long conversions;
    private Long cost;
    private Double ctr;
    private Double cvr;
    private Double cpa;
    private Double cpc;
    private Double cpm;
    private Instant createdAt;
    private Instant updatedAt;

    public static DailyMetricsResponse from(DailyMetrics metrics) {
        return DailyMetricsResponse.builder()
            .id(metrics.getId())
            .date(metrics.getDate())
            .adId(metrics.getAdId())
            .adGroupId(metrics.getAdGroupId())
            .campaignId(metrics.getCampaignId())
            .impressions(metrics.getImpressions())
            .clicks(metrics.getClicks())
            .conversions(metrics.getConversions())
            .cost(metrics.getCost())
            .ctr(metrics.ctr())
            .cvr(metrics.cvr())
            .cpa(metrics.cpa())
            .cpc(metrics.cpc())
            .cpm(metrics.cpm())
            .createdAt(metrics.getCreatedAt())
            .updatedAt(metrics.getUpdatedAt())
            .build();
    }
}
