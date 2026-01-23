package com.adplatform.metrics.adapter.rest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adplatform.metrics.application.dto.CampaignMetricsDto;
import com.adplatform.metrics.application.usecase.GetMetricsByAdUseCase;
import com.adplatform.metrics.application.usecase.GetMetricsByCampaignUseCase;
import com.adplatform.metrics.domain.model.DailyMetrics;
import com.adplatform.metrics.domain.repository.DailyMetricsRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MetricsController 통합 테스트
 */
@WebMvcTest(MetricsController.class)
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetMetricsByAdUseCase getMetricsByAdUseCase;

    @MockBean
    private GetMetricsByCampaignUseCase getMetricsByCampaignUseCase;

    @MockBean
    private DailyMetricsRepository metricsRepository;

    @MockBean
    private com.adplatform.metrics.application.usecase.AggregateMetricsUseCase aggregateMetricsUseCase;

    @Test
    void 광고별_메트릭스_조회_API_성공() throws Exception {
        // given
        String adId = "ad-123";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        List<DailyMetrics> mockMetrics = Arrays.asList(
            createMetrics(
                "metrics-1",
                adId,
                LocalDate.of(2024, 1, 1),
                1000L,
                50L,
                5L,
                50000L
            )
        );

        when(
            getMetricsByAdUseCase.execute(adId, startDate, endDate)
        ).thenReturn(mockMetrics);

        // when & then
        mockMvc
            .perform(
                get("/api/v1/metrics/ad/{adId}", adId)
                    .param("startDate", "2024-01-01")
                    .param("endDate", "2024-01-31")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].adId").value(adId))
            .andExpect(jsonPath("$[0].impressions").value(1000))
            .andExpect(jsonPath("$[0].clicks").value(50))
            .andExpect(jsonPath("$[0].ctr").value(5.0));
    }

    @Test
    void 캠페인별_메트릭스_조회_API_성공() throws Exception {
        // given
        String campaignId = "campaign-123";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        CampaignMetricsDto mockDto = CampaignMetricsDto.builder()
            .campaignId(campaignId)
            .startDate(startDate)
            .endDate(endDate)
            .totalImpressions(10000L)
            .totalClicks(500L)
            .totalConversions(50L)
            .totalCost(500000L)
            .ctr(5.0)
            .cvr(10.0)
            .cpa(10000.0)
            .dailyMetrics(List.of())
            .build();

        when(
            getMetricsByCampaignUseCase.execute(campaignId, startDate, endDate)
        ).thenReturn(mockDto);

        // when & then
        mockMvc
            .perform(
                get("/api/v1/metrics/campaign/{campaignId}", campaignId)
                    .param("startDate", "2024-01-01")
                    .param("endDate", "2024-01-31")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.campaignId").value(campaignId))
            .andExpect(jsonPath("$.totalImpressions").value(10000))
            .andExpect(jsonPath("$.totalClicks").value(500))
            .andExpect(jsonPath("$.ctr").value(5.0));
    }

    @Test
    void 일일_메트릭스_조회_API_성공() throws Exception {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);

        List<DailyMetrics> mockMetrics = Arrays.asList(
            createMetrics("metrics-1", "ad-1", date, 1000L, 50L, 5L, 50000L),
            createMetrics("metrics-2", "ad-2", date, 2000L, 100L, 10L, 100000L)
        );

        when(metricsRepository.findByDate(date)).thenReturn(mockMetrics);

        // when & then
        mockMvc
            .perform(get("/api/v1/metrics/daily").param("date", "2024-01-15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].adId").value("ad-1"))
            .andExpect(jsonPath("$[1].adId").value("ad-2"));
    }

    @Test
    void Health_체크_API() throws Exception {
        mockMvc
            .perform(get("/api/v1/metrics/health"))
            .andExpect(status().isOk());
    }

    private DailyMetrics createMetrics(
        String id,
        String adId,
        LocalDate date,
        Long impressions,
        Long clicks,
        Long conversions,
        Long cost
    ) {
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
