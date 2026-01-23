package com.adplatform.billing.domain.exception;

/**
 * 거래를 찾을 수 없을 때 발생하는 예외
 */
public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String transactionId) {
        super("거래를 찾을 수 없습니다: " + transactionId);
    }
}
