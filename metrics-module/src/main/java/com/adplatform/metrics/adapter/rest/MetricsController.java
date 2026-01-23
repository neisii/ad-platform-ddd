package com.adplatform.metrics.adapter.rest;

import com.adplatform.metrics.adapter.rest.dto.AggregateMetricsRequest;
import com.adplatform.metrics.adapter.rest.dto.CampaignMetricsResponse;
import com.adplatform.metrics.adapter.rest.dto.DailyMetricsResponse;
import com.adplatform.metrics.application.dto.CampaignMetricsDto;
import com.adplatform.metrics.application.usecase.AggregateMetricsUseCase;
import com.adplatform.metrics.application.usecase.GetMetricsByAdUseCase;
import com.adplatform.metrics.application.usecase.GetMetricsByCampaignUseCase;
import com.adplatform.metrics.domain.model.DailyMetrics;
import com.adplatform.metrics.domain.repository.DailyMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 메트릭스 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final AggregateMetricsUseCase aggregateMetricsUseCase;
    private final GetMetricsByAdUseCase getMetricsByAdUseCase;
    private final GetMetricsByCampaignUseCase getMetricsByCampaignUseCase;
    private final DailyMetricsRepository metricsRepository;

    /**
     * 메트릭스 집계 트리거 (비동기)
     * POST /api/v1/metrics/aggregate
     */
    @PostMapping("/aggregate")
    public ResponseEntity<String> aggregateMetrics(@RequestBody AggregateMetricsRequest request) {
        log.info("메트릭스 집계 요청: {}", request);

        // 비동기 처리 (별도 스레드에서 실행)
        new Thread(() -> {
            try {
                if (request.getDate() != null) {
                    aggregateMetricsUseCase.aggregateByDate(request.getDate());
                } else if (request.getStartDate() != null && request.getEndDate() != null) {
                    aggregateMetricsUseCase.aggregateByDateRange(
                        request.getStartDate(),
                        request.getEndDate()
                    );
                } else {
                    // 기본값: 어제
                    LocalDate yesterday = LocalDate.now().minusDays(1);
                    aggregateMetricsUseCase.aggregateByDate(yesterday);
                }
            } catch (Exception e) {
                log.error("메트릭스 집계 실패", e);
            }
        }).start();

        return ResponseEntity.accepted().body("메트릭스 집계가 시작되었습니다");
    }

    /**
     * 광고별 메트릭스 조회
     * GET /api/v1/metrics/ad/{adId}?startDate={startDate}&endDate={endDate}
     */
    @GetMapping("/ad/{adId}")
    public ResponseEntity<List<DailyMetricsResponse>> getMetricsByAd(
        @PathVariable String adId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("광고 메트릭스 조회: adId={}, startDate={}, endDate={}", adId, startDate, endDate);

        List<DailyMetrics> metrics = getMetricsByAdUseCase.execute(adId, startDate, endDate);

        List<DailyMetricsResponse> response = metrics.stream()
            .map(DailyMetricsResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 캠페인별 메트릭스 조회 (롤업)
     * GET /api/v1/metrics/campaign/{campaignId}?startDate={startDate}&endDate={endDate}
     */
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<CampaignMetricsResponse> getMetricsByCampaign(
        @PathVariable String campaignId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("캠페인 메트릭스 조회: campaignId={}, startDate={}, endDate={}",
            campaignId, startDate, endDate);

        CampaignMetricsDto dto = getMetricsByCampaignUseCase.execute(campaignId, startDate, endDate);

        return ResponseEntity.ok(CampaignMetricsResponse.from(dto));
    }

    /**
     * 특정 날짜의 모든 메트릭스 조회
     * GET /api/v1/metrics/daily?date={date}
     */
    @GetMapping("/daily")
    public ResponseEntity<List<DailyMetricsResponse>> getDailyMetrics(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("일일 메트릭스 조회: date={}", date);

        List<DailyMetrics> metrics = metricsRepository.findByDate(date);

        List<DailyMetricsResponse> response = metrics.stream()
            .map(DailyMetricsResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Health Check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Metrics Service is running");
    }
}
