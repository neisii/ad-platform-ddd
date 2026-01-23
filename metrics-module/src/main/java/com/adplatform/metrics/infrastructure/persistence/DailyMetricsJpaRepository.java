package com.adplatform.metrics.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DailyMetrics Spring Data JPA Repository
 */
public interface DailyMetricsJpaRepository extends JpaRepository<DailyMetricsEntity, String> {

    /**
     * 날짜와 광고 ID로 조회
     */
    Optional<DailyMetricsEntity> findByDateAndAdId(LocalDate date, String adId);

    /**
     * 광고 ID와 날짜 범위로 조회
     */
    @Query("SELECT m FROM DailyMetricsEntity m WHERE m.adId = :adId " +
           "AND m.date BETWEEN :startDate AND :endDate ORDER BY m.date")
    List<DailyMetricsEntity> findByAdIdAndDateRange(
        @Param("adId") String adId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 캠페인 ID와 날짜 범위로 조회
     */
    @Query("SELECT m FROM DailyMetricsEntity m WHERE m.campaignId = :campaignId " +
           "AND m.date BETWEEN :startDate AND :endDate ORDER BY m.date")
    List<DailyMetricsEntity> findByCampaignIdAndDateRange(
        @Param("campaignId") String campaignId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 광고그룹 ID와 날짜 범위로 조회
     */
    @Query("SELECT m FROM DailyMetricsEntity m WHERE m.adGroupId = :adGroupId " +
           "AND m.date BETWEEN :startDate AND :endDate ORDER BY m.date")
    List<DailyMetricsEntity> findByAdGroupIdAndDateRange(
        @Param("adGroupId") String adGroupId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 특정 날짜의 모든 메트릭스 조회
     */
    List<DailyMetricsEntity> findByDate(LocalDate date);

    /**
     * 존재 여부 확인
     */
    boolean existsByDateAndAdId(LocalDate date, String adId);
}
