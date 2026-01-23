package com.adplatform.eventlog.infrastructure.persistence;

import com.adplatform.eventlog.domain.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * AdEvent JPA Repository 인터페이스
 */
public interface AdEventJpaRepository extends JpaRepository<AdEventEntity, String> {

    /**
     * 광고 ID로 이벤트 목록 조회
     */
    List<AdEventEntity> findByAdId(String adId);

    /**
     * 시간 범위로 이벤트 목록 조회
     */
    @Query("SELECT e FROM AdEventEntity e WHERE e.timestamp >= :startTime AND e.timestamp <= :endTime ORDER BY e.timestamp ASC")
    List<AdEventEntity> findByTimestampBetween(
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * 캠페인 ID로 이벤트 목록 조회
     */
    List<AdEventEntity> findByCampaignId(String campaignId);

    /**
     * 이벤트 타입으로 조회
     */
    List<AdEventEntity> findByEventType(EventType eventType);
}
