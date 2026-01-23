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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetEventsByAdUseCase 테스트")
class GetEventsByAdUseCaseTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private GetEventsByAdUseCase getEventsByAdUseCase;

    private List<AdEvent> events;

    @BeforeEach
    void setUp() {
        AdEvent event1 = AdEvent.builder()
            .id("event-1")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();

        AdEvent event2 = AdEvent.builder()
            .id("event-2")
            .eventType(EventType.CLICK)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .impressionToken("impression-token-1")
            .build();

        events = Arrays.asList(event1, event2);
    }

    @Test
    @DisplayName("광고 ID로 이벤트 목록 조회 성공")
    void getEventsByAdIdSuccessfully() {
        // given
        String adId = "ad-456";
        when(eventRepository.findByAdId(adId)).thenReturn(events);

        // when
        List<AdEvent> result = getEventsByAdUseCase.execute(adId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AdEvent::getAdId)
            .containsOnly("ad-456");
        verify(eventRepository).findByAdId(adId);
    }

    @Test
    @DisplayName("존재하지 않는 광고 ID로 조회 시 빈 목록 반환")
    void returnEmptyListWhenNoEventsFound() {
        // given
        String adId = "non-existent-ad";
        when(eventRepository.findByAdId(adId)).thenReturn(List.of());

        // when
        List<AdEvent> result = getEventsByAdUseCase.execute(adId);

        // then
        assertThat(result).isEmpty();
        verify(eventRepository).findByAdId(adId);
    }

    @Test
    @DisplayName("null 광고 ID로 조회 시 예외 발생")
    void throwExceptionWhenAdIdIsNull() {
        // when & then
        assertThatThrownBy(() -> getEventsByAdUseCase.execute(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("광고 ID는 필수입니다");

        verify(eventRepository, never()).findByAdId(any());
    }

    @Test
    @DisplayName("빈 문자열 광고 ID로 조회 시 예외 발생")
    void throwExceptionWhenAdIdIsEmpty() {
        // when & then
        assertThatThrownBy(() -> getEventsByAdUseCase.execute("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("광고 ID는 필수입니다");

        verify(eventRepository, never()).findByAdId(any());
    }
}
