package com.adplatform.metrics.domain.repository;

import com.adplatform.metrics.domain.model.DailyMetrics;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DailyMetrics Aggregate Repository
 * - Domain layer의 인터페이스
 * - Infrastructure layer에서 구현
 */
public interface DailyMetricsRepository {

    /**
     * 메트릭스 저장 (Upsert)
     * - date + adId가 동일하면 업데이트
     * - 없으면 새로 생성
     */
    DailyMetrics save(DailyMetrics metrics);

    /**
     * ID로 메트릭스 조회
     */
    Optional<DailyMetrics> findById(String id);

    /**
     * 날짜와 광고 ID로 메트릭스 조회
     */
    Optional<DailyMetrics> findByDateAndAdId(LocalDate date, String adId);

    /**
     * 광고 ID와 날짜 범위로 메트릭스 조회
     */
    List<DailyMetrics> findByAdIdAndDateRange(String adId, LocalDate startDate, LocalDate endDate);

    /**
     * 캠페인 ID와 날짜 범위로 메트릭스 조회
     */
    List<DailyMetrics> findByCampaignIdAndDateRange(String campaignId, LocalDate startDate, LocalDate endDate);

    /**
     * 광고그룹 ID와 날짜 범위로 메트릭스 조회
     */
    List<DailyMetrics> findByAdGroupIdAndDateRange(String adGroupId, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 날짜의 모든 메트릭스 조회
     */
    List<DailyMetrics> findByDate(LocalDate date);

    /**
     * 메트릭스 존재 여부 확인 (날짜 + 광고 ID)
     */
    boolean existsByDateAndAdId(LocalDate date, String adId);

    /**
     * 메트릭스 삭제
     */
    void deleteById(String id);
}
