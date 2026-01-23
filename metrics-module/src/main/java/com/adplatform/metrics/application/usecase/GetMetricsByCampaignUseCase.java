package com.adplatform.metrics.application.usecase;

import com.adplatform.metrics.application.dto.CampaignMetricsDto;
import com.adplatform.metrics.domain.model.DailyMetrics;
import com.adplatform.metrics.domain.repository.DailyMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 캠페인별 메트릭스 조회 Use Case
 * - 캠페인의 모든 광고 메트릭스를 롤업하여 집계
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMetricsByCampaignUseCase {

    private final DailyMetricsRepository metricsRepository;

    /**
     * 캠페인 ID와 날짜 범위로 메트릭스 롤업 조회
     */
    public CampaignMetricsDto execute(String campaignId, LocalDate startDate, LocalDate endDate) {
        log.debug("캠페인 메트릭스 조회: campaignId={}, startDate={}, endDate={}",
            campaignId, startDate, endDate);

        List<DailyMetrics> dailyMetrics = metricsRepository
            .findByCampaignIdAndDateRange(campaignId, startDate, endDate);

        return CampaignMetricsDto.fromDailyMetrics(campaignId, startDate, endDate, dailyMetrics);
    }
}
