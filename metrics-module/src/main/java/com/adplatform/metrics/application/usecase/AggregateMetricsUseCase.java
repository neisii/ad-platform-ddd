package com.adplatform.metrics.application.usecase;

import com.adplatform.metrics.domain.model.DailyMetrics;
import com.adplatform.metrics.domain.model.PricingModel;
import com.adplatform.metrics.domain.repository.DailyMetricsRepository;
import com.adplatform.metrics.domain.service.MetricsCalculator;
import com.adplatform.metrics.infrastructure.client.CampaignClient;
import com.adplatform.metrics.infrastructure.client.EventLogClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 메트릭스 집계 Use Case
 * - 이벤트 로그를 집계하여 일일 메트릭스 생성
 * - 배치 작업으로 주기적으로 실행 (매 시간)
 * - Idempotent: 동일한 날짜에 대해 여러 번 실행해도 결과 동일
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AggregateMetricsUseCase {

    private final DailyMetricsRepository metricsRepository;
    private final EventLogClient eventLogClient;
    private final CampaignClient campaignClient;
    private final MetricsCalculator metricsCalculator;

    /**
     * 특정 날짜의 이벤트를 집계
     */
    public void aggregateByDate(LocalDate date) {
        log.info("메트릭스 집계 시작: {}", date);

        // 1. 날짜별 이벤트 조회
        List<EventLogClient.AdEventDto> events = eventLogClient.getEventsByDate(date);

        if (events.isEmpty()) {
            log.info("집계할 이벤트가 없습니다: {}", date);
            return;
        }

        // 2. 광고별로 그룹화
        Map<String, List<EventLogClient.AdEventDto>> eventsByAd = events.stream()
            .collect(Collectors.groupingBy(EventLogClient.AdEventDto::getAdId));

        // 3. 각 광고별로 메트릭스 집계
        eventsByAd.forEach((adId, adEvents) -> {
            try {
                aggregateForAd(date, adId, adEvents);
            } catch (Exception e) {
                log.error("광고 메트릭스 집계 실패: adId={}, date={}", adId, date, e);
            }
        });

        log.info("메트릭스 집계 완료: {}, 광고 수={}", date, eventsByAd.size());
    }

    /**
     * 날짜 범위의 이벤트를 집계
     */
    public void aggregateByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("메트릭스 집계 시작 (범위): {} ~ {}", startDate, endDate);

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            aggregateByDate(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        log.info("메트릭스 집계 완료 (범위): {} ~ {}", startDate, endDate);
    }

    /**
     * 특정 광고의 메트릭스 집계
     */
    private void aggregateForAd(LocalDate date, String adId, List<EventLogClient.AdEventDto> events) {
        if (events.isEmpty()) {
            return;
        }

        // 첫 번째 이벤트에서 메타정보 추출
        EventLogClient.AdEventDto firstEvent = events.get(0);
        String adGroupId = firstEvent.getAdGroupId();
        String campaignId = firstEvent.getCampaignId();

        // 광고그룹 정보 조회 (입찰가, 가격 모델)
        CampaignClient.AdGroupDto adGroup = campaignClient.getAdGroup(adGroupId);
        Long bid = adGroup.getBid();
        PricingModel pricingModel = adGroup.getPricingModel() != null
            ? adGroup.getPricingModel()
            : PricingModel.CPC;

        // 기존 메트릭스 조회
        Optional<DailyMetrics> existingMetrics = metricsRepository.findByDateAndAdId(date, adId);

        DailyMetrics newMetrics;
        if (existingMetrics.isPresent()) {
            // 기존 메트릭스에 집계
            DailyMetrics existing = existingMetrics.get();
            DailyMetrics additional = metricsCalculator.aggregateEvents(
                existing.getId(), events, pricingModel, bid
            );

            newMetrics = existing.aggregate(
                additional.getImpressions(),
                additional.getClicks(),
                additional.getConversions(),
                additional.getCost()
            );
        } else {
            // 신규 메트릭스 생성
            String metricsId = generateMetricsId();
            newMetrics = metricsCalculator.aggregateEvents(
                metricsId, events, pricingModel, bid
            );
        }

        // 저장 (Upsert)
        metricsRepository.save(newMetrics);
        log.debug("메트릭스 저장 완료: adId={}, date={}, impressions={}, clicks={}, conversions={}",
            adId, date, newMetrics.getImpressions(), newMetrics.getClicks(), newMetrics.getConversions());
    }

    private String generateMetricsId() {
        return "metrics-" + UUID.randomUUID().toString();
    }
}
