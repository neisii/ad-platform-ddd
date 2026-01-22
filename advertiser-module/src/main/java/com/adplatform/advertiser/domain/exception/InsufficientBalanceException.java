package com.adplatform.advertiser.domain.exception;

/**
 * 잔액 부족 예외
 */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }

    public static InsufficientBalanceException withAmount(Long required, Long current) {
        return new InsufficientBalanceException(
            String.format("잔액이 부족합니다. 필요: %d, 현재: %d", required, current)
        );
    }
}
