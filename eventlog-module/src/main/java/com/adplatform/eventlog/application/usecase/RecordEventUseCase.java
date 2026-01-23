package com.adplatform.eventlog.application.usecase;

import com.adplatform.eventlog.application.dto.RecordEventCommand;
import com.adplatform.eventlog.domain.model.AdEvent;
import com.adplatform.eventlog.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이벤트 기록 유스케이스
 * - Idempotent: 동일 ID로 중복 요청 시 기존 이벤트 반환
 * - Append-only: 이벤트는 추가만 가능
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecordEventUseCase {

    private final EventRepository eventRepository;

    @Transactional
    public AdEvent execute(RecordEventCommand command) {
        log.info("Recording event: id={}, type={}, adId={}",
            command.getId(), command.getEventType(), command.getAdId());

        // Idempotent 처리: 이미 존재하면 기존 이벤트 반환
        if (eventRepository.existsById(command.getId())) {
            log.info("Event already exists: id={}, returning existing event", command.getId());
            return eventRepository.findById(command.getId())
                .orElseThrow(() -> new IllegalStateException("이벤트가 존재하지만 조회할 수 없습니다"));
        }

        // 새 이벤트 생성
        AdEvent event = AdEvent.builder()
            .id(command.getId())
            .eventType(command.getEventType())
            .adId(command.getAdId())
            .campaignId(command.getCampaignId())
            .adGroupId(command.getAdGroupId())
            .userId(command.getUserId())
            .sessionId(command.getSessionId())
            .timestamp(command.getTimestamp())
            .metadata(command.getMetadata())
            .impressionToken(command.getImpressionToken())
            .build();

        // 저장
        AdEvent saved = eventRepository.save(event);
        log.info("Event recorded successfully: id={}", saved.getId());

        return saved;
    }
}
