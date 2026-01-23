package com.adplatform.eventlog.application.usecase;

import com.adplatform.eventlog.domain.model.AdEvent;
import com.adplatform.eventlog.domain.model.EventType;
import com.adplatform.eventlog.domain.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetEventsByTimeRangeUseCase 테스트")
class GetEventsByTimeRangeUseCaseTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private GetEventsByTimeRangeUseCase getEventsByTimeRangeUseCase;

    private Instant startTime;
    private Instant endTime;
    private List<AdEvent> events;

    @BeforeEach
    void setUp() {
        startTime = Instant.now().minus(1, ChronoUnit.HOURS);
        endTime = Instant.now();

        AdEvent event1 = AdEvent.builder()
            .id("event-1")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(startTime.plus(10, ChronoUnit.MINUTES))
            .build();

        AdEvent event2 = AdEvent.builder()
            .id("event-2")
            .eventType(EventType.CLICK)
            .adId("ad-789")
            .campaignId("campaign-789")
            .timestamp(startTime.plus(30, ChronoUnit.MINUTES))
            .impressionToken("impression-token-1")
            .build();

        events = Arrays.asList(event1, event2);
    }

    @Test
    @DisplayName("시간 범위로 이벤트 목록 조회 성공")
    void getEventsByTimeRangeSuccessfully() {
        // given
        when(eventRepository.findByTimeRange(startTime, endTime)).thenReturn(events);

        // when
        List<AdEvent> result = getEventsByTimeRangeUseCase.execute(startTime, endTime);

        // then
        assertThat(result).hasSize(2);
        verify(eventRepository).findByTimeRange(startTime, endTime);
    }

    @Test
    @DisplayName("존재하지 않는 시간 범위로 조회 시 빈 목록 반환")
    void returnEmptyListWhenNoEventsInTimeRange() {
        // given
        when(eventRepository.findByTimeRange(startTime, endTime)).thenReturn(List.of());

        // when
        List<AdEvent> result = getEventsByTimeRangeUseCase.execute(startTime, endTime);

        // then
        assertThat(result).isEmpty();
        verify(eventRepository).findByTimeRange(startTime, endTime);
    }

    @Test
    @DisplayName("startTime이 null이면 예외 발생")
    void throwExceptionWhenStartTimeIsNull() {
        // when & then
        assertThatThrownBy(() -> getEventsByTimeRangeUseCase.execute(null, endTime))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("시작 시간은 필수입니다");

        verify(eventRepository, never()).findByTimeRange(any(), any());
    }

    @Test
    @DisplayName("endTime이 null이면 예외 발생")
    void throwExceptionWhenEndTimeIsNull() {
        // when & then
        assertThatThrownBy(() -> getEventsByTimeRangeUseCase.execute(startTime, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("종료 시간은 필수입니다");

        verify(eventRepository, never()).findByTimeRange(any(), any());
    }

    @Test
    @DisplayName("startTime이 endTime보다 이후이면 예외 발생")
    void throwExceptionWhenStartTimeIsAfterEndTime() {
        // given
        Instant invalidStartTime = endTime.plus(1, ChronoUnit.HOURS);

        // when & then
        assertThatThrownBy(() -> getEventsByTimeRangeUseCase.execute(invalidStartTime, endTime))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("시작 시간은 종료 시간보다 이전이어야 합니다");

        verify(eventRepository, never()).findByTimeRange(any(), any());
    }
}
