package com.adplatform.billing.domain.exception;

/**
 * 잘못된 거래 상태일 때 발생하는 예외
 */
public class InvalidTransactionStateException extends RuntimeException {
    public InvalidTransactionStateException(String message) {
        super(message);
    }
}
