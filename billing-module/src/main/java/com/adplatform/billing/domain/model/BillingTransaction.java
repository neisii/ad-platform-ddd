package com.adplatform.billing.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 청구 거래 Aggregate Root
 * - 일일 메트릭스 기반 청구
 * - 광고주 잔액 차감 관리
 * - 거래 상태 추적 (PENDING, COMPLETED, FAILED, REFUNDED)
 * - 비즈니스 규칙: dailyMetricsId 고유 제약 (중복 청구 방지)
 */
@Getter
public class BillingTransaction {

    private final String id;
    private final String advertiserId;
    private final String campaignId;
    private final String dailyMetricsId;
    private final Long amount;
    private final TransactionStatus status;
    private final LocalDate billingDate;
    private final Instant processedAt;
    private final String description;
    private final Instant createdAt;

    @Builder
    public BillingTransaction(
        String id,
        String advertiserId,
        String campaignId,
        String dailyMetricsId,
        Long amount,
        TransactionStatus status,
        LocalDate billingDate,
        Instant processedAt,
        String description,
        Instant createdAt
    ) {
        validateAdvertiserId(advertiserId);
        validateCampaignId(campaignId);
        validateDailyMetricsId(dailyMetricsId);
        validateAmount(amount);
        validateStatus(status);
        validateBillingDate(billingDate);

        this.id = id;
        this.advertiserId = advertiserId;
        this.campaignId = campaignId;
        this.dailyMetricsId = dailyMetricsId;
        this.amount = amount;
        this.status = status;
        this.billingDate = billingDate;
        this.processedAt = processedAt;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    private void validateAdvertiserId(String advertiserId) {
        if (advertiserId == null || advertiserId.trim().isEmpty()) {
            throw new IllegalArgumentException("광고주 ID는 필수입니다");
        }
    }

    private void validateCampaignId(String campaignId) {
        if (campaignId == null || campaignId.trim().isEmpty()) {
            throw new IllegalArgumentException("캠페인 ID는 필수입니다");
        }
    }

    private void validateDailyMetricsId(String dailyMetricsId) {
        if (dailyMetricsId == null || dailyMetricsId.trim().isEmpty()) {
            throw new IllegalArgumentException("일일 메트릭스 ID는 필수입니다");
        }
    }

    private void validateAmount(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("거래 금액은 0보다 커야 합니다");
        }
    }

    private void validateStatus(TransactionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("거래 상태는 필수입니다");
        }
    }

    private void validateBillingDate(LocalDate billingDate) {
        if (billingDate == null) {
            throw new IllegalArgumentException("청구 날짜는 필수입니다");
        }
    }

    /**
     * 거래를 완료 상태로 표시
     * - 잔액 차감 성공 후 호출
     */
    public BillingTransaction markAsCompleted() {
        return BillingTransaction.builder()
            .id(this.id)
            .advertiserId(this.advertiserId)
            .campaignId(this.campaignId)
            .dailyMetricsId(this.dailyMetricsId)
            .amount(this.amount)
            .status(TransactionStatus.COMPLETED)
            .billingDate(this.billingDate)
            .processedAt(Instant.now())
            .description(this.description)
            .createdAt(this.createdAt)
            .build();
    }

    /**
     * 거래를 실패 상태로 표시
     * - 잔액 차감 실패 후 호출 (잔액 부족 등)
     */
    public BillingTransaction markAsFailed() {
        return BillingTransaction.builder()
            .id(this.id)
            .advertiserId(this.advertiserId)
            .campaignId(this.campaignId)
            .dailyMetricsId(this.dailyMetricsId)
            .amount(this.amount)
            .status(TransactionStatus.FAILED)
            .billingDate(this.billingDate)
            .processedAt(Instant.now())
            .description(this.description)
            .createdAt(this.createdAt)
            .build();
    }

    /**
     * 거래 환불
     * - COMPLETED 상태의 거래만 환불 가능
     * - 광고주 잔액 복구 필요
     */
    public BillingTransaction refund() {
        if (!this.status.canRefund()) {
            throw new IllegalStateException("COMPLETED 상태의 거래만 환불할 수 있습니다");
        }

        return BillingTransaction.builder()
            .id(this.id)
            .advertiserId(this.advertiserId)
            .campaignId(this.campaignId)
            .dailyMetricsId(this.dailyMetricsId)
            .amount(this.amount)
            .status(TransactionStatus.REFUNDED)
            .billingDate(this.billingDate)
            .processedAt(Instant.now())
            .description(this.description)
            .createdAt(this.createdAt)
            .build();
    }

    /**
     * 동일한 일일 메트릭스 ID 확인
     * - 중복 청구 방지 (idempotency)
     */
    public boolean hasDailyMetricsId(String dailyMetricsId) {
        return this.dailyMetricsId.equals(dailyMetricsId);
    }

    /**
     * 재시도 가능 여부
     */
    public boolean canRetry() {
        return this.status.canRetry();
    }

    /**
     * 환불 가능 여부
     */
    public boolean canRefund() {
        return this.status.canRefund();
    }
}
