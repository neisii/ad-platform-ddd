package com.adplatform.eventlog.application.usecase;

import com.adplatform.eventlog.domain.model.AdEvent;
import com.adplatform.eventlog.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 시간 범위로 이벤트 조회 유스케이스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetEventsByTimeRangeUseCase {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<AdEvent> execute(Instant startTime, Instant endTime) {
        log.info("Getting events by time range: {} to {}", startTime, endTime);

        validateTimeRange(startTime, endTime);

        List<AdEvent> events = eventRepository.findByTimeRange(startTime, endTime);
        log.info("Found {} events in time range", events.size());

        return events;
    }

    private void validateTimeRange(Instant startTime, Instant endTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("시작 시간은 필수입니다");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("종료 시간은 필수입니다");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다");
        }
    }
}
