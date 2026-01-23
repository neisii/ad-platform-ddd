package com.adplatform.metrics.domain.service;

import static org.assertj.core.api.Assertions.*;

import com.adplatform.metrics.domain.model.DailyMetrics;
import com.adplatform.metrics.domain.model.PricingModel;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * MetricsCalculator 도메인 서비스 테스트
 */
class MetricsCalculatorTest {

    private MetricsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new MetricsCalculator();
    }

    @Test
    void 이벤트_집계_성공() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        String adId = "ad-123";
        String adGroupId = "adgroup-123";
        String campaignId = "campaign-123";

        List<AdEventData> events = Arrays.asList(
            createEvent("IMPRESSION", adId, adGroupId, campaignId, date),
            createEvent("IMPRESSION", adId, adGroupId, campaignId, date),
            createEvent("IMPRESSION", adId, adGroupId, campaignId, date),
            createEvent("CLICK", adId, adGroupId, campaignId, date),
            createEvent("CLICK", adId, adGroupId, campaignId, date),
            createEvent("CONVERSION", adId, adGroupId, campaignId, date)
        );

        Long bid = 1000L;
        PricingModel pricingModel = PricingModel.CPC;

        // when
        DailyMetrics metrics = calculator.aggregateEvents(
            "metrics-1",
            events,
            pricingModel,
            bid
        );

        // then
        assertThat(metrics.getDate()).isEqualTo(date);
        assertThat(metrics.getAdId()).isEqualTo(adId);
        assertThat(metrics.getAdGroupId()).isEqualTo(adGroupId);
        assertThat(metrics.getCampaignId()).isEqualTo(campaignId);
        assertThat(metrics.getImpressions()).isEqualTo(3L);
        assertThat(metrics.getClicks()).isEqualTo(2L);
        assertThat(metrics.getConversions()).isEqualTo(1L);
        assertThat(metrics.getCost()).isEqualTo(2000L); // CPC * clicks = 1000 * 2
    }

    @Test
    void CPM_비용_계산() {
        // given
        PricingModel pricingModel = PricingModel.CPM;
        Long bid = 5000L; // 1000 노출당 5000원
        Long eventCount = 2500L; // 2500 노출

        // when
        Long cost = calculator.calculateCost(pricingModel, bid, eventCount);

        // then
        assertThat(cost).isEqualTo(12500L); // 5000 * 2500 / 1000 = 12500
    }

    @Test
    void CPC_비용_계산() {
        // given
        PricingModel pricingModel = PricingModel.CPC;
        Long bid = 1000L; // 클릭당 1000원
        Long eventCount = 50L; // 50 클릭

        // when
        Long cost = calculator.calculateCost(pricingModel, bid, eventCount);

        // then
        assertThat(cost).isEqualTo(50000L); // 1000 * 50 = 50000
    }

    @Test
    void CPA_비용_계산() {
        // given
        PricingModel pricingModel = PricingModel.CPA;
        Long bid = 10000L; // 전환당 10000원
        Long eventCount = 5L; // 5 전환

        // when
        Long cost = calculator.calculateCost(pricingModel, bid, eventCount);

        // then
        assertThat(cost).isEqualTo(50000L); // 10000 * 5 = 50000
    }

    @Test
    void 이벤트_카운트_0일때_비용_0() {
        // when
        Long costCPM = calculator.calculateCost(PricingModel.CPM, 5000L, 0L);
        Long costCPC = calculator.calculateCost(PricingModel.CPC, 1000L, 0L);
        Long costCPA = calculator.calculateCost(PricingModel.CPA, 10000L, 0L);

        // then
        assertThat(costCPM).isEqualTo(0L);
        assertThat(costCPC).isEqualTo(0L);
        assertThat(costCPA).isEqualTo(0L);
    }

    @Test
    void 빈_이벤트_리스트_집계시_제로_메트릭스() {
        // given
        List<AdEventData> events = Arrays.asList();
        LocalDate date = LocalDate.of(2024, 1, 15);

        // when
        DailyMetrics metrics = calculator.aggregateEvents(
            "metrics-1",
            events,
            date,
            "ad-123",
            "adgroup-123",
            "campaign-123",
            PricingModel.CPC,
            1000L
        );

        // then
        assertThat(metrics.getImpressions()).isEqualTo(0L);
        assertThat(metrics.getClicks()).isEqualTo(0L);
        assertThat(metrics.getConversions()).isEqualTo(0L);
        assertThat(metrics.getCost()).isEqualTo(0L);
    }

    @Test
    void 여러_광고의_이벤트_혼합시_올바르게_필터링() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        String targetAdId = "ad-123";

        List<AdEventData> events = Arrays.asList(
            createEvent(
                "IMPRESSION",
                targetAdId,
                "adgroup-123",
                "campaign-123",
                date
            ),
            createEvent(
                "IMPRESSION",
                "ad-456",
                "adgroup-456",
                "campaign-123",
                date
            ), // 다른 광고
            createEvent(
                "CLICK",
                targetAdId,
                "adgroup-123",
                "campaign-123",
                date
            ),
            createEvent("CLICK", "ad-456", "adgroup-456", "campaign-123", date) // 다른 광고
        );

        // when
        DailyMetrics metrics = calculator.aggregateEvents(
            "metrics-1",
            events,
            PricingModel.CPC,
            1000L
        );

        // then - targetAdId의 이벤트만 집계되어야 함
        assertThat(metrics.getAdId()).isEqualTo(targetAdId);
        assertThat(metrics.getImpressions()).isEqualTo(1L);
        assertThat(metrics.getClicks()).isEqualTo(1L);
    }

    // Helper method
    private AdEventData createEvent(
        String eventType,
        String adId,
        String adGroupId,
        String campaignId,
        LocalDate date
    ) {
        return new AdEventData(
            "event-" + System.nanoTime(),
            eventType,
            adId,
            adGroupId,
            campaignId,
            date,
            Instant.now()
        );
    }

    // Inner class for test data
    public static class AdEventData implements MetricsCalculator.EventData {

        private final String id;
        private final String eventType;
        private final String adId;
        private final String adGroupId;
        private final String campaignId;
        private final LocalDate date;
        private final Instant timestamp;

        public AdEventData(
            String id,
            String eventType,
            String adId,
            String adGroupId,
            String campaignId,
            LocalDate date,
            Instant timestamp
        ) {
            this.id = id;
            this.eventType = eventType;
            this.adId = adId;
            this.adGroupId = adGroupId;
            this.campaignId = campaignId;
            this.date = date;
            this.timestamp = timestamp;
        }

        public String getId() {
            return id;
        }

        @Override
        public String getEventType() {
            return eventType;
        }

        @Override
        public String getAdId() {
            return adId;
        }

        @Override
        public String getAdGroupId() {
            return adGroupId;
        }

        @Override
        public String getCampaignId() {
            return campaignId;
        }

        @Override
        public LocalDate getDate() {
            return date;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }
}
