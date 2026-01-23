package com.adplatform.eventlog.domain.repository;

import com.adplatform.eventlog.domain.model.AdEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 이벤트 저장소 인터페이스 (Port)
 * - Append-only: 이벤트는 추가만 가능, 수정/삭제 불가
 */
public interface EventRepository {
    /**
     * 이벤트 저장 (Idempotent - 중복 ID 처리)
     */
    AdEvent save(AdEvent event);

    /**
     * ID로 이벤트 조회
     */
    Optional<AdEvent> findById(String id);

    /**
     * 광고 ID로 이벤트 목록 조회
     */
    List<AdEvent> findByAdId(String adId);

    /**
     * 시간 범위로 이벤트 목록 조회
     */
    List<AdEvent> findByTimeRange(Instant startTime, Instant endTime);

    /**
     * ID 존재 여부 확인 (중복 방지용)
     */
    boolean existsById(String id);
}
