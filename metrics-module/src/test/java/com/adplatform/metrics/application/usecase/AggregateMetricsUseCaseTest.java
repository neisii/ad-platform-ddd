package com.adplatform.metrics.application.usecase;

import com.adplatform.metrics.domain.model.DailyMetrics;
import com.adplatform.metrics.domain.model.PricingModel;
import com.adplatform.metrics.domain.repository.DailyMetricsRepository;
import com.adplatform.metrics.domain.service.MetricsCalculator;
import com.adplatform.metrics.infrastructure.client.CampaignClient;
import com.adplatform.metrics.infrastructure.client.EventLogClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AggregateMetricsUseCase 테스트
 */
@ExtendWith(MockitoExtension.class)
class AggregateMetricsUseCaseTest {

    @Mock
    private DailyMetricsRepository metricsRepository;

    @Mock
    private EventLogClient eventLogClient;

    @Mock
    private CampaignClient campaignClient;

    private MetricsCalculator metricsCalculator;
    private AggregateMetricsUseCase useCase;

    @BeforeEach
    void setUp() {
        metricsCalculator = new MetricsCalculator();
        useCase = new AggregateMetricsUseCase(
            metricsRepository,
            eventLogClient,
            campaignClient,
            metricsCalculator
        );
    }

    @Test
    void 날짜별_메트릭스_집계_성공() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<EventLogClient.AdEventDto> events = createTestEvents(date);

        CampaignClient.AdGroupDto adGroup = new CampaignClient.AdGroupDto();
        adGroup.setBid(1000L);
        adGroup.setPricingModel(PricingModel.CPC);

        when(eventLogClient.getEventsByDate(date)).thenReturn(events);
        when(campaignClient.getAdGroup(anyString())).thenReturn(adGroup);
        when(metricsRepository.findByDateAndAdId(any(), anyString())).thenReturn(Optional.empty());
        when(metricsRepository.save(any(DailyMetrics.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        useCase.aggregateByDate(date);

        // then
        verify(eventLogClient).getEventsByDate(date);
        verify(metricsRepository, atLeastOnce()).save(any(DailyMetrics.class));
    }

    @Test
    void 기존_메트릭스가_있으면_집계하여_업데이트() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        String adId = "ad-123";

        DailyMetrics existingMetrics = DailyMetrics.builder()
            .id("metrics-1")
            .date(date)
            .adId(adId)
            .adGroupId("adgroup-123")
            .campaignId("campaign-123")
            .impressions(100L)
            .clicks(10L)
            .conversions(1L)
            .cost(10000L)
            .build();

        List<EventLogClient.AdEventDto> newEvents = Arrays.asList(
            createEvent("IMPRESSION", adId, "adgroup-123", "campaign-123", date),
            createEvent("CLICK", adId, "adgroup-123", "campaign-123", date)
        );

        CampaignClient.AdGroupDto adGroup = new CampaignClient.AdGroupDto();
        adGroup.setBid(1000L);
        adGroup.setPricingModel(PricingModel.CPC);

        when(eventLogClient.getEventsByDate(date)).thenReturn(newEvents);
        when(campaignClient.getAdGroup(anyString())).thenReturn(adGroup);
        when(metricsRepository.findByDateAndAdId(date, adId)).thenReturn(Optional.of(existingMetrics));
        when(metricsRepository.save(any(DailyMetrics.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        useCase.aggregateByDate(date);

        // then
        verify(metricsRepository).save(argThat(metrics ->
            metrics.getImpressions() == 101L && // 100 + 1
            metrics.getClicks() == 11L // 10 + 1
        ));
    }

    @Test
    void 이벤트가_없으면_집계하지_않음() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(eventLogClient.getEventsByDate(date)).thenReturn(List.of());

        // when
        useCase.aggregateByDate(date);

        // then
        verify(metricsRepository, never()).save(any());
    }

    private List<EventLogClient.AdEventDto> createTestEvents(LocalDate date) {
        return Arrays.asList(
            createEvent("IMPRESSION", "ad-123", "adgroup-123", "campaign-123", date),
            createEvent("IMPRESSION", "ad-123", "adgroup-123", "campaign-123", date),
            createEvent("CLICK", "ad-123", "adgroup-123", "campaign-123", date),
            createEvent("CONVERSION", "ad-123", "adgroup-123", "campaign-123", date)
        );
    }

    private EventLogClient.AdEventDto createEvent(String eventType, String adId,
                                                   String adGroupId, String campaignId, LocalDate date) {
        EventLogClient.AdEventDto event = new EventLogClient.AdEventDto();
        event.setId("event-" + System.nanoTime());
        event.setEventType(eventType);
        event.setAdId(adId);
        event.setAdGroupId(adGroupId);
        event.setCampaignId(campaignId);
        event.setTimestamp(date.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
        return event;
    }
}
