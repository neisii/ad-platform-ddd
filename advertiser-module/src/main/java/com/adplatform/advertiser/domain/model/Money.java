package com.adplatform.advertiser.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 금액 Value Object
 * - 불변 객체
 * - 통화 단위 포함 (기본: KRW)
 */
@Getter
@EqualsAndHashCode
public class Money {
    private final Long amount;
    private final String currency;

    private Money(Long amount, String currency) {
        validateAmount(amount);
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(Long amount) {
        return new Money(amount, "KRW");
    }

    public static Money of(Long amount, String currency) {
        return new Money(amount, currency);
    }

    private void validateAmount(Long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다");
        }
    }

    /**
     * 금액 더하기
     */
    public Money add(Long value) {
        return new Money(this.amount + value, this.currency);
    }

    /**
     * 금액 빼기
     */
    public Money subtract(Long value) {
        long newAmount = this.amount - value;
        if (newAmount < 0) {
            throw new IllegalArgumentException("차감 후 금액이 음수가 될 수 없습니다");
        }
        return new Money(newAmount, this.currency);
    }

    /**
     * 특정 금액을 지불할 수 있는지 확인
     */
    public boolean canAfford(Long value) {
        return this.amount >= value;
    }

    /**
     * 잔액 부족 여부
     */
    public boolean isInsufficient(Long value) {
        return this.amount < value;
    }
}
