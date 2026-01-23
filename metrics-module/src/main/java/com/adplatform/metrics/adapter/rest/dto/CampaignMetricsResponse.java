package com.adplatform.metrics.adapter.rest.dto;

import com.adplatform.metrics.application.dto.CampaignMetricsDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 캠페인 메트릭스 응답 DTO
 */
@Getter
@Builder
public class CampaignMetricsResponse {
    private String campaignId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalImpressions;
    private Long totalClicks;
    private Long totalConversions;
    private Long totalCost;
    private Double ctr;
    private Double cvr;
    private Double cpa;
    private List<DailyMetricsResponse> dailyMetrics;

    public static CampaignMetricsResponse from(CampaignMetricsDto dto) {
        return CampaignMetricsResponse.builder()
            .campaignId(dto.getCampaignId())
            .startDate(dto.getStartDate())
            .endDate(dto.getEndDate())
            .totalImpressions(dto.getTotalImpressions())
            .totalClicks(dto.getTotalClicks())
            .totalConversions(dto.getTotalConversions())
            .totalCost(dto.getTotalCost())
            .ctr(dto.getCtr())
            .cvr(dto.getCvr())
            .cpa(dto.getCpa())
            .dailyMetrics(dto.getDailyMetrics().stream()
                .map(DailyMetricsResponse::from)
                .collect(Collectors.toList()))
            .build();
    }
}
