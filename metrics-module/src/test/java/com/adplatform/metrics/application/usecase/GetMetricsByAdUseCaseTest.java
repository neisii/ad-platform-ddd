package com.adplatform.metrics.application.usecase;

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
 * GetMetricsByAdUseCase 테스트
 */
@ExtendWith(MockitoExtension.class)
class GetMetricsByAdUseCaseTest {

    @Mock
    private DailyMetricsRepository metricsRepository;

    private GetMetricsByAdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetMetricsByAdUseCase(metricsRepository);
    }

    @Test
    void 광고별_메트릭스_조회_성공() {
        // given
        String adId = "ad-123";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        List<DailyMetrics> mockMetrics = Arrays.asList(
            createMetrics("metrics-1", adId, LocalDate.of(2024, 1, 1), 1000L, 50L, 5L, 50000L),
            createMetrics("metrics-2", adId, LocalDate.of(2024, 1, 2), 2000L, 100L, 10L, 100000L)
        );

        when(metricsRepository.findByAdIdAndDateRange(adId, startDate, endDate))
            .thenReturn(mockMetrics);

        // when
        List<DailyMetrics> result = useCase.execute(adId, startDate, endDate);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAdId()).isEqualTo(adId);
        assertThat(result.get(1).getAdId()).isEqualTo(adId);
        verify(metricsRepository).findByAdIdAndDateRange(adId, startDate, endDate);
    }

    @Test
    void 메트릭스가_없으면_빈_리스트_반환() {
        // given
        String adId = "ad-123";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        when(metricsRepository.findByAdIdAndDateRange(adId, startDate, endDate))
            .thenReturn(List.of());

        // when
        List<DailyMetrics> result = useCase.execute(adId, startDate, endDate);

        // then
        assertThat(result).isEmpty();
    }

    private DailyMetrics createMetrics(String id, String adId, LocalDate date,
                                        Long impressions, Long clicks, Long conversions, Long cost) {
        return DailyMetrics.builder()
            .id(id)
            .date(date)
            .adId(adId)
            .adGroupId("adgroup-123")
            .campaignId("campaign-123")
            .impressions(impressions)
            .clicks(clicks)
            .conversions(conversions)
            .cost(cost)
            .build();
    }
}
