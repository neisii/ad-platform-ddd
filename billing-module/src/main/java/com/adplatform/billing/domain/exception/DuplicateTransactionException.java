package com.adplatform.billing.domain.exception;

/**
 * 중복 거래 시도 시 발생하는 예외
 * - dailyMetricsId가 이미 청구된 경우
 */
public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String dailyMetricsId) {
        super("이미 청구된 메트릭스입니다: " + dailyMetricsId);
    }
}
