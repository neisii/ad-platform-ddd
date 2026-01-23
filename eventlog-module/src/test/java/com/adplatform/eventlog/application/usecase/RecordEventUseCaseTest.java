package com.adplatform.eventlog.application.usecase;

import com.adplatform.eventlog.application.dto.RecordEventCommand;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordEventUseCase 테스트")
class RecordEventUseCaseTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private RecordEventUseCase recordEventUseCase;

    private RecordEventCommand command;
    private AdEvent event;

    @BeforeEach
    void setUp() {
        command = RecordEventCommand.builder()
            .id("event-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();

        event = AdEvent.builder()
            .id("event-123")
            .eventType(EventType.IMPRESSION)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();
    }

    @Test
    @DisplayName("새 이벤트를 성공적으로 기록")
    void recordNewEventSuccessfully() {
        // given
        when(eventRepository.existsById(command.getId())).thenReturn(false);
        when(eventRepository.save(any(AdEvent.class))).thenReturn(event);

        // when
        AdEvent result = recordEventUseCase.execute(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("event-123");
        verify(eventRepository).existsById(command.getId());
        verify(eventRepository).save(any(AdEvent.class));
    }

    @Test
    @DisplayName("중복 이벤트 기록 시 기존 이벤트 반환 (Idempotent)")
    void returnExistingEventWhenDuplicate() {
        // given
        when(eventRepository.existsById(command.getId())).thenReturn(true);
        when(eventRepository.findById(command.getId())).thenReturn(Optional.of(event));

        // when
        AdEvent result = recordEventUseCase.execute(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("event-123");
        verify(eventRepository).existsById(command.getId());
        verify(eventRepository).findById(command.getId());
        verify(eventRepository, never()).save(any(AdEvent.class));
    }

    @Test
    @DisplayName("CLICK 이벤트를 impressionToken과 함께 기록")
    void recordClickEventWithImpressionToken() {
        // given
        RecordEventCommand clickCommand = RecordEventCommand.builder()
            .id("event-click-123")
            .eventType(EventType.CLICK)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .impressionToken("impression-token-123")
            .build();

        AdEvent clickEvent = AdEvent.builder()
            .id("event-click-123")
            .eventType(EventType.CLICK)
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .impressionToken("impression-token-123")
            .build();

        when(eventRepository.existsById(clickCommand.getId())).thenReturn(false);
        when(eventRepository.save(any(AdEvent.class))).thenReturn(clickEvent);

        // when
        AdEvent result = recordEventUseCase.execute(clickCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEventType()).isEqualTo(EventType.CLICK);
        assertThat(result.getImpressionToken()).isEqualTo("impression-token-123");
        verify(eventRepository).save(any(AdEvent.class));
    }

    @Test
    @DisplayName("유효하지 않은 이벤트 데이터로 기록 시도 시 예외 발생")
    void throwExceptionWhenInvalidEventData() {
        // given
        RecordEventCommand invalidCommand = RecordEventCommand.builder()
            .id("event-123")
            .eventType(null)  // Invalid: null eventType
            .adId("ad-456")
            .campaignId("campaign-789")
            .timestamp(Instant.now())
            .build();

        when(eventRepository.existsById(invalidCommand.getId())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> recordEventUseCase.execute(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class);

        verify(eventRepository, never()).save(any(AdEvent.class));
    }
}
