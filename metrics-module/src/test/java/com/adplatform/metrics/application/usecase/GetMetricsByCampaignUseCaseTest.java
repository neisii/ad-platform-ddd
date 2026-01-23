package com.adplatform.metrics.application.usecase;

import com.adplatform.metrics.application.dto.CampaignMetricsDto;
import com.adplatform.metrics.domain.model.DailyMetrics;
import com.adplatform.metrics.domain.repository.DailyMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GetMetricsByCampaignUseCase 테스트
 */
@ExtendWith(MockitoExtension.class)
class GetMetricsByCampaignUseCaseTest {

    @Mock
    private DailyMetricsRepository metricsRepository;

    private GetMetricsByCampaignUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetMetricsByCampaignUseCase(metricsRepository);
    }

    @Test
    void 캠페인별_메트릭스_롤업_성공() {
        // given
        String campaignId = "campaign-123";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 3);

        List<DailyMetrics> mockMetrics = Arrays.asList(
            createMetrics("metrics-1", "ad-1", campaignId, LocalDate.of(2024, 1, 1), 1000L, 50L, 5L, 50000L),
            createMetrics("metrics-2", "ad-2", campaignId, LocalDate.of(2024, 1, 1), 2000L, 100L, 10L, 100000L),
            createMetrics("metrics-3", "ad-1", campaignId, LocalDate.of(2024, 1, 2), 1500L, 75L, 8L, 75000L)
        );

        when(metricsRepository.findByCampaignIdAndDateRange(campaignId, startDate, endDate))
            .thenReturn(mockMetrics);

        // when
        CampaignMetricsDto result = useCase.execute(campaignId, startDate, endDate);

        // then
        assertThat(result.getCampaignId()).isEqualTo(campaignId);
        assertThat(result.getTotalImpressions()).isEqualTo(4500L); // 1000 + 2000 + 1500
        assertThat(result.getTotalClicks()).isEqualTo(225L); // 50 + 100 + 75
        assertThat(result.getTotalConversions()).isEqualTo(23L); // 5 + 10 + 8
        assertThat(result.getTotalCost()).isEqualTo(225000L); // 50000 + 100000 + 75000
        assertThat(result.getCtr()).isEqualTo(5.0); // 225 / 4500 * 100
        assertThat(result.getDailyMetrics()).hasSize(3);
    }

    @Test
    void 메트릭스가_없으면_제로_메트릭스_반환() {
        // given
        String campaignId = "campaign-123";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        when(metricsRepository.findByCampaignIdAndDateRange(campaignId, startDate, endDate))
            .thenReturn(List.of());

        // when
        CampaignMetricsDto result = useCase.execute(campaignId, startDate, endDate);

        // then
        assertThat(result.getCampaignId()).isEqualTo(campaignId);
        assertThat(result.getTotalImpressions()).isEqualTo(0L);
        assertThat(result.getTotalClicks()).isEqualTo(0L);
        assertThat(result.getTotalConversions()).isEqualTo(0L);
        assertThat(result.getTotalCost()).isEqualTo(0L);
        assertThat(result.getCtr()).isEqualTo(0.0);
        assertThat(result.getDailyMetrics()).isEmpty();
    }

    @Test
    void CTR_CVR_CPA_계산_정확성() {
        // given
        String campaignId = "campaign-123";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);

        List<DailyMetrics> mockMetrics = Arrays.asList(
            createMetrics("metrics-1", "ad-1", campaignId, LocalDate.of(2024, 1, 1), 10000L, 500L, 50L, 500000L)
        );

        when(metricsRepository.findByCampaignIdAndDateRange(campaignId, startDate, endDate))
            .thenReturn(mockMetrics);

        // when
        CampaignMetricsDto result = useCase.execute(campaignId, startDate, endDate);

        // then
        assertThat(result.getCtr()).isEqualTo(5.0); // 500 / 10000 * 100 = 5.0
        assertThat(result.getCvr()).isEqualTo(10.0); // 50 / 500 * 100 = 10.0
        assertThat(result.getCpa()).isEqualTo(10000.0); // 500000 / 50 = 10000.0
    }

    private DailyMetrics createMetrics(String id, String adId, String campaignId, LocalDate date,
                                        Long impressions, Long clicks, Long conversions, Long cost) {
        return DailyMetrics.builder()
            .id(id)
            .date(date)
            .adId(adId)
            .adGroupId("adgroup-123")
            .campaignId(campaignId)
            .impressions(impressions)
            .clicks(clicks)
            .conversions(conversions)
            .cost(cost)
            .build();
    }
}
