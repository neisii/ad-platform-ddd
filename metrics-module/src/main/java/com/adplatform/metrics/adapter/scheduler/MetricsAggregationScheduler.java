package com.adplatform.metrics.adapter.scheduler;

import com.adplatform.metrics.application.usecase.AggregateMetricsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 메트릭스 집계 스케줄러
 * - 매 시간마다 이전 시간의 이벤트를 집계
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsAggregationScheduler {

    private final AggregateMetricsUseCase aggregateMetricsUseCase;

    /**
     * 매 시간 정각에 실행 (cron: "0 0 * * * *")
     * - 어제 날짜의 메트릭스를 집계
     */
    @Scheduled(cron = "0 0 * * * *")
    public void aggregateHourly() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        log.info("===== 스케줄된 메트릭스 집계 시작 =====");
        log.info("집계 날짜: {}", yesterday);

        try {
            aggregateMetricsUseCase.aggregateByDate(yesterday);
            log.info("===== 메트릭스 집계 완료 =====");
        } catch (Exception e) {
            log.error("메트릭스 집계 실패", e);
        }
    }

    /**
     * 매일 자정에 실행 (cron: "0 0 0 * * *")
     * - 전날의 메트릭스를 최종 집계
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void aggregateDaily() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        log.info("===== 일일 메트릭스 최종 집계 시작 =====");
        log.info("집계 날짜: {}", yesterday);

        try {
            aggregateMetricsUseCase.aggregateByDate(yesterday);
            log.info("===== 일일 메트릭스 최종 집계 완료 =====");
        } catch (Exception e) {
            log.error("일일 메트릭스 집계 실패", e);
        }
    }
}
