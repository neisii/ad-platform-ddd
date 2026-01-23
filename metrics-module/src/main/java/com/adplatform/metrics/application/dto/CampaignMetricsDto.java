package com.adplatform.metrics.application.dto;

import com.adplatform.metrics.domain.model.DailyMetrics;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 캠페인 메트릭스 집계 DTO
 * - 캠페인 전체의 롤업 메트릭스
 */
@Getter
@Builder
public class CampaignMetricsDto {
    private final String campaignId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Long totalImpressions;
    private final Long totalClicks;
    private final Long totalConversions;
    private final Long totalCost;
    private final Double ctr;
    private final Double cvr;
    private final Double cpa;
    private final List<DailyMetrics> dailyMetrics;

    /**
     * DailyMetrics 리스트로부터 집계 생성
     */
    public static CampaignMetricsDto fromDailyMetrics(String campaignId, LocalDate startDate,
                                                       LocalDate endDate, List<DailyMetrics> metrics) {
        if (metrics.isEmpty()) {
            return CampaignMetricsDto.builder()
                .campaignId(campaignId)
                .startDate(startDate)
                .endDate(endDate)
                .totalImpressions(0L)
                .totalClicks(0L)
                .totalConversions(0L)
                .totalCost(0L)
                .ctr(0.0)
                .cvr(0.0)
                .cpa(0.0)
                .dailyMetrics(List.of())
                .build();
        }

        long totalImpressions = metrics.stream()
            .mapToLong(DailyMetrics::getImpressions)
            .sum();

        long totalClicks = metrics.stream()
            .mapToLong(DailyMetrics::getClicks)
            .sum();

        long totalConversions = metrics.stream()
            .mapToLong(DailyMetrics::getConversions)
            .sum();

        long totalCost = metrics.stream()
            .mapToLong(DailyMetrics::getCost)
            .sum();

        double ctr = totalImpressions > 0 ? (double) totalClicks / totalImpressions * 100 : 0.0;
        double cvr = totalClicks > 0 ? (double) totalConversions / totalClicks * 100 : 0.0;
        double cpa = totalConversions > 0 ? (double) totalCost / totalConversions : 0.0;

        return CampaignMetricsDto.builder()
            .campaignId(campaignId)
            .startDate(startDate)
            .endDate(endDate)
            .totalImpressions(totalImpressions)
            .totalClicks(totalClicks)
            .totalConversions(totalConversions)
            .totalCost(totalCost)
            .ctr(ctr)
            .cvr(cvr)
            .cpa(cpa)
            .dailyMetrics(metrics)
            .build();
    }
}
