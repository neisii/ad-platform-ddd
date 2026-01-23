package com.adplatform.billing.domain.repository;

import com.adplatform.billing.domain.model.BillingTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * BillingTransaction Repository Interface
 * - 도메인 계층의 포트
 */
public interface BillingTransactionRepository {

    /**
     * 거래 저장
     */
    BillingTransaction save(BillingTransaction transaction);

    /**
     * ID로 거래 조회
     */
    Optional<BillingTransaction> findById(String id);

    /**
     * 일일 메트릭스 ID로 거래 조회
     * - 중복 청구 방지용
     */
    Optional<BillingTransaction> findByDailyMetricsId(String dailyMetricsId);

    /**
     * 광고주별 거래 목록 조회
     */
    List<BillingTransaction> findByAdvertiserId(String advertiserId);

    /**
     * 캠페인별 거래 목록 조회
     */
    List<BillingTransaction> findByCampaignId(String campaignId);

    /**
     * 기간별 거래 목록 조회
     */
    List<BillingTransaction> findByBillingDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 광고주 및 기간별 거래 목록 조회
     */
    List<BillingTransaction> findByAdvertiserIdAndBillingDateBetween(
        String advertiserId, LocalDate startDate, LocalDate endDate);

    /**
     * 일일 메트릭스 ID 존재 여부 확인
     * - 중복 청구 방지용
     */
    boolean existsByDailyMetricsId(String dailyMetricsId);
}
