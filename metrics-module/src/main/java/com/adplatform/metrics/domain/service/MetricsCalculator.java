package com.adplatform.metrics.domain.service;

import com.adplatform.metrics.domain.model.DailyMetrics;
import com.adplatform.metrics.domain.model.PricingModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 메트릭스 계산 도메인 서비스
 * - 이벤트를 집계하여 DailyMetrics 생성
 * - 가격 모델에 따른 비용 계산
 */
public class MetricsCalculator {

    /**
     * 이벤트 리스트를 집계하여 DailyMetrics 생성
     *
     * @param id 메트릭스 ID
     * @param events 이벤트 리스트
     * @param pricingModel 가격 모델
     * @param bid 입찰가
     * @return 집계된 DailyMetrics
     */
    public <T extends EventData> DailyMetrics aggregateEvents(
        String id,
        List<T> events,
        PricingModel pricingModel,
        Long bid
    ) {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("이벤트 리스트가 비어있습니다");
        }

        // 첫 번째 이벤트에서 기본 정보 추출
        T firstEvent = events.get(0);
        String adId = firstEvent.getAdId();
        String adGroupId = firstEvent.getAdGroupId();
        String campaignId = firstEvent.getCampaignId();
        LocalDate date = firstEvent.getDate();

        // 해당 광고의 이벤트만 필터링
        List<T> filteredEvents = events.stream()
            .filter(e -> e.getAdId().equals(adId))
            .collect(Collectors.toList());

        // 이벤트 타입별로 카운트
        Map<String, Long> eventCounts = filteredEvents.stream()
            .collect(Collectors.groupingBy(
                EventData::getEventType,
                Collectors.counting()
            ));

        Long impressions = eventCounts.getOrDefault("IMPRESSION", 0L);
        Long clicks = eventCounts.getOrDefault("CLICK", 0L);
        Long conversions = eventCounts.getOrDefault("CONVERSION", 0L);

        // 비용 계산 (가격 모델에 따라)
        Long cost = calculateCostByModel(pricingModel, bid, impressions, clicks, conversions);

        return DailyMetrics.builder()
            .id(id)
            .date(date)
            .adId(adId)
            .adGroupId(adGroupId)
            .campaignId(campaignId)
            .impressions(impressions)
            .clicks(clicks)
            .conversions(conversions)
            .cost(cost)
            .build();
    }

    /**
     * 빈 이벤트로 제로 메트릭스 생성 (오버로드)
     */
    public <T extends EventData> DailyMetrics aggregateEvents(
        String id,
        List<T> events,
        LocalDate date,
        String adId,
        String adGroupId,
        String campaignId,
        PricingModel pricingModel,
        Long bid
    ) {
        if (events == null || events.isEmpty()) {
            return DailyMetrics.builder()
                .id(id)
                .date(date)
                .adId(adId)
                .adGroupId(adGroupId)
                .campaignId(campaignId)
                .impressions(0L)
                .clicks(0L)
                .conversions(0L)
                .cost(0L)
                .build();
        }

        return aggregateEvents(id, events, pricingModel, bid);
    }

    /**
     * 가격 모델에 따른 비용 계산
     */
    private Long calculateCostByModel(PricingModel pricingModel, Long bid,
                                       Long impressions, Long clicks, Long conversions) {
        return switch (pricingModel) {
            case CPM -> calculateCost(pricingModel, bid, impressions);
            case CPC -> calculateCost(pricingModel, bid, clicks);
            case CPA -> calculateCost(pricingModel, bid, conversions);
        };
    }

    /**
     * 가격 모델과 이벤트 수에 따른 비용 계산
     *
     * @param pricingModel 가격 모델
     * @param bid 입찰가
     * @param eventCount 이벤트 수
     * @return 계산된 비용
     */
    public Long calculateCost(PricingModel pricingModel, Long bid, Long eventCount) {
        if (eventCount == null || eventCount == 0) {
            return 0L;
        }

        if (bid == null || bid <= 0) {
            throw new IllegalArgumentException("입찰가는 0보다 커야 합니다");
        }

        return switch (pricingModel) {
            case CPM -> calculateCPMCost(bid, eventCount);
            case CPC -> bid * eventCount;
            case CPA -> bid * eventCount;
        };
    }

    /**
     * CPM 비용 계산 (1000 노출당 비용)
     */
    private Long calculateCPMCost(Long bid, Long impressions) {
        return (bid * impressions) / 1000;
    }

    /**
     * 이벤트 데이터 인터페이스
     * - 테스트와 실제 이벤트 모두 사용 가능하도록 추상화
     */
    public interface EventData {
        String getEventType();
        String getAdId();
        String getAdGroupId();
        String getCampaignId();
        LocalDate getDate();
    }
}
