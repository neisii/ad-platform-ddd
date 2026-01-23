package com.adplatform.metrics.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * DailyMetrics 도메인 모델 테스트
 */
class DailyMetricsTest {

    @Test
    void 일일_메트릭스_생성_성공() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        String adId = "ad-123";
        String adGroupId = "adgroup-123";
        String campaignId = "campaign-123";

        // when
        DailyMetrics metrics = DailyMetrics.builder()
            .id("metrics-1")
            .date(date)
            .adId(adId)
            .adGroupId(adGroupId)
            .campaignId(campaignId)
            .impressions(1000L)
            .clicks(50L)
            .conversions(5L)
            .cost(10000L)
            .build();

        // then
        assertThat(metrics.getId()).isEqualTo("metrics-1");
        assertThat(metrics.getDate()).isEqualTo(date);
        assertThat(metrics.getAdId()).isEqualTo(adId);
        assertThat(metrics.getAdGroupId()).isEqualTo(adGroupId);
        assertThat(metrics.getCampaignId()).isEqualTo(campaignId);
        assertThat(metrics.getImpressions()).isEqualTo(1000L);
        assertThat(metrics.getClicks()).isEqualTo(50L);
        assertThat(metrics.getConversions()).isEqualTo(5L);
        assertThat(metrics.getCost()).isEqualTo(10000L);
    }

    @Test
    void CTR_계산_성공() {
        // given
        DailyMetrics metrics = createMetrics(1000L, 50L, 5L, 10000L);

        // when
        double ctr = metrics.ctr();

        // then
        assertThat(ctr).isEqualTo(5.0); // 50 / 1000 * 100 = 5.0%
    }

    @Test
    void CTR_계산_노출수_0일때() {
        // given
        DailyMetrics metrics = createMetrics(0L, 0L, 0L, 0L);

        // when
        double ctr = metrics.ctr();

        // then
        assertThat(ctr).isEqualTo(0.0);
    }

    @Test
    void CVR_계산_성공() {
        // given
        DailyMetrics metrics = createMetrics(1000L, 50L, 5L, 10000L);

        // when
        double cvr = metrics.cvr();

        // then
        assertThat(cvr).isEqualTo(10.0); // 5 / 50 * 100 = 10.0%
    }

    @Test
    void CVR_계산_클릭수_0일때() {
        // given
        DailyMetrics metrics = createMetrics(1000L, 0L, 0L, 0L);

        // when
        double cvr = metrics.cvr();

        // then
        assertThat(cvr).isEqualTo(0.0);
    }

    @Test
    void CPA_계산_성공() {
        // given
        DailyMetrics metrics = createMetrics(1000L, 50L, 5L, 10000L);

        // when
        double cpa = metrics.cpa();

        // then
        assertThat(cpa).isEqualTo(2000.0); // 10000 / 5 = 2000.0
    }

    @Test
    void CPA_계산_전환수_0일때() {
        // given
        DailyMetrics metrics = createMetrics(1000L, 50L, 0L, 10000L);

        // when
        double cpa = metrics.cpa();

        // then
        assertThat(cpa).isEqualTo(0.0);
    }

    @Test
    void 날짜는_필수() {
        assertThatThrownBy(() ->
            DailyMetrics.builder()
                .id("metrics-1")
                .date(null)
                .adId("ad-123")
                .adGroupId("adgroup-123")
                .campaignId("campaign-123")
                .impressions(100L)
                .clicks(10L)
                .conversions(1L)
                .cost(1000L)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("날짜는 필수입니다");
    }

    @Test
    void 광고_ID는_필수() {
        assertThatThrownBy(() ->
            DailyMetrics.builder()
                .id("metrics-1")
                .date(LocalDate.now())
                .adId(null)
                .adGroupId("adgroup-123")
                .campaignId("campaign-123")
                .impressions(100L)
                .clicks(10L)
                .conversions(1L)
                .cost(1000L)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("광고 ID는 필수입니다");
    }

    @Test
    void 캠페인_ID는_필수() {
        assertThatThrownBy(() ->
            DailyMetrics.builder()
                .id("metrics-1")
                .date(LocalDate.now())
                .adId("ad-123")
                .adGroupId("adgroup-123")
                .campaignId(null)
                .impressions(100L)
                .clicks(10L)
                .conversions(1L)
                .cost(1000L)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("캠페인 ID는 필수입니다");
    }

    @Test
    void 노출수는_음수_불가() {
        assertThatThrownBy(() ->
            DailyMetrics.builder()
                .id("metrics-1")
                .date(LocalDate.now())
                .adId("ad-123")
                .adGroupId("adgroup-123")
                .campaignId("campaign-123")
                .impressions(-1L)
                .clicks(10L)
                .conversions(1L)
                .cost(1000L)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("노출수는 0 이상이어야 합니다");
    }

    @Test
    void 클릭수는_노출수를_초과할_수_없음() {
        assertThatThrownBy(() ->
            DailyMetrics.builder()
                .id("metrics-1")
                .date(LocalDate.now())
                .adId("ad-123")
                .adGroupId("adgroup-123")
                .campaignId("campaign-123")
                .impressions(10L)
                .clicks(20L)
                .conversions(1L)
                .cost(1000L)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("클릭수는 노출수를 초과할 수 없습니다");
    }

    @Test
    void 전환수는_클릭수를_초과할_수_없음() {
        assertThatThrownBy(() ->
            DailyMetrics.builder()
                .id("metrics-1")
                .date(LocalDate.now())
                .adId("ad-123")
                .adGroupId("adgroup-123")
                .campaignId("campaign-123")
                .impressions(100L)
                .clicks(10L)
                .conversions(20L)
                .cost(1000L)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("전환수는 클릭수를 초과할 수 없습니다");
    }

    @Test
    void 메트릭스_집계_성공() {
        // given
        DailyMetrics existing = createMetrics(1000L, 50L, 5L, 10000L);

        // when
        DailyMetrics aggregated = existing.aggregate(500L, 25L, 3L, 5000L);

        // then
        assertThat(aggregated.getImpressions()).isEqualTo(1500L);
        assertThat(aggregated.getClicks()).isEqualTo(75L);
        assertThat(aggregated.getConversions()).isEqualTo(8L);
        assertThat(aggregated.getCost()).isEqualTo(15000L);
    }

    @Test
    void 동일한_날짜와_광고_ID_확인() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        String adId = "ad-123";

        DailyMetrics metrics = DailyMetrics.builder()
            .id("metrics-1")
            .date(date)
            .adId(adId)
            .adGroupId("adgroup-123")
            .campaignId("campaign-123")
            .impressions(100L)
            .clicks(10L)
            .conversions(1L)
            .cost(1000L)
            .build();

        // when & then
        assertThat(metrics.isSameDateAndAd(date, adId)).isTrue();
        assertThat(metrics.isSameDateAndAd(date, "ad-456")).isFalse();
        assertThat(metrics.isSameDateAndAd(LocalDate.of(2024, 1, 16), adId)).isFalse();
    }

    private DailyMetrics createMetrics(Long impressions, Long clicks, Long conversions, Long cost) {
        return DailyMetrics.builder()
            .id("metrics-1")
            .date(LocalDate.of(2024, 1, 15))
            .adId("ad-123")
            .adGroupId("adgroup-123")
            .campaignId("campaign-123")
            .impressions(impressions)
            .clicks(clicks)
            .conversions(conversions)
            .cost(cost)
            .build();
    }
}
