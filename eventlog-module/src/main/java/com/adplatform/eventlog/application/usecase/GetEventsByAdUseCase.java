package com.adplatform.eventlog.application.usecase;

import com.adplatform.eventlog.domain.model.AdEvent;
import com.adplatform.eventlog.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 광고 ID로 이벤트 조회 유스케이스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetEventsByAdUseCase {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<AdEvent> execute(String adId) {
        log.info("Getting events by adId: {}", adId);

        if (adId == null || adId.trim().isEmpty()) {
            throw new IllegalArgumentException("광고 ID는 필수입니다");
        }

        List<AdEvent> events = eventRepository.findByAdId(adId);
        log.info("Found {} events for adId: {}", events.size(), adId);

        return events;
    }
}
