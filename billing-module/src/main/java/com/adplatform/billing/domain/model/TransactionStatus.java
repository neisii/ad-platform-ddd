package com.adplatform.billing.domain.model;

/**
 * 거래 상태
 * - PENDING: 처리 대기
 * - COMPLETED: 처리 완료
 * - FAILED: 처리 실패
 * - REFUNDED: 환불 완료
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED;

    /**
     * 재시도 가능 여부
     * - PENDING, FAILED 상태만 재시도 가능
     */
    public boolean canRetry() {
        return this == PENDING || this == FAILED;
    }

    /**
     * 환불 가능 여부
     * - COMPLETED 상태만 환불 가능
     */
    public boolean canRefund() {
        return this == COMPLETED;
    }
}
