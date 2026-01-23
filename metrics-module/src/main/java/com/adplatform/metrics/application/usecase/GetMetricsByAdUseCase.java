package com.adplatform.metrics.application.usecase;

import com.adplatform.metrics.domain.model.DailyMetrics;
import com.adplatform.metrics.domain.repository.DailyMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 광고별 메트릭스 조회 Use Case
 * - 특정 광고의 날짜 범위별 메트릭스 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMetricsByAdUseCase {

    private final DailyMetricsRepository metricsRepository;

    /**
     * 광고 ID와 날짜 범위로 메트릭스 조회
     */
    public List<DailyMetrics> execute(String adId, LocalDate startDate, LocalDate endDate) {
        log.debug("광고 메트릭스 조회: adId={}, startDate={}, endDate={}", adId, startDate, endDate);

        return metricsRepository.findByAdIdAndDateRange(adId, startDate, endDate);
    }
}
