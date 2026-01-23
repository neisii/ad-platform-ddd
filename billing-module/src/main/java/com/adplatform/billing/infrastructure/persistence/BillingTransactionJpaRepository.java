package com.adplatform.billing.infrastructure.persistence;

import com.adplatform.billing.domain.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * BillingTransaction JPA Repository
 */
public interface BillingTransactionJpaRepository extends JpaRepository<BillingTransactionEntity, String> {

    /**
     * 일일 메트릭스 ID로 거래 조회
     */
    Optional<BillingTransactionEntity> findByDailyMetricsId(String dailyMetricsId);

    /**
     * 광고주별 거래 목록 조회
     */
    List<BillingTransactionEntity> findByAdvertiserIdOrderByBillingDateDesc(String advertiserId);

    /**
     * 캠페인별 거래 목록 조회
     */
    List<BillingTransactionEntity> findByCampaignIdOrderByBillingDateDesc(String campaignId);

    /**
     * 기간별 거래 목록 조회
     */
    List<BillingTransactionEntity> findByBillingDateBetweenOrderByBillingDateDesc(
        LocalDate startDate, LocalDate endDate);

    /**
     * 광고주 및 기간별 거래 목록 조회
     */
    List<BillingTransactionEntity> findByAdvertiserIdAndBillingDateBetweenOrderByBillingDateDesc(
        String advertiserId, LocalDate startDate, LocalDate endDate);

    /**
     * 일일 메트릭스 ID 존재 여부 확인
     */
    boolean existsByDailyMetricsId(String dailyMetricsId);

    /**
     * 상태별 거래 목록 조회
     */
    List<BillingTransactionEntity> findByStatusOrderByBillingDateDesc(TransactionStatus status);
}
