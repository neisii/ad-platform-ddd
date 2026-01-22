package com.adplatform.campaign.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 예산 Value Object
 * - 불변 객체
 * - 일예산/총예산 검증 로직 캡슐화
 */
@Getter
@EqualsAndHashCode
public class Budget {
    private final Long dailyBudget;
    private final Long totalBudget;
    private final Long spent;

    public Budget(Long dailyBudget, Long totalBudget) {
        this(dailyBudget, totalBudget, 0L);
    }

    public Budget(Long dailyBudget, Long totalBudget, Long spent) {
        validateBudget(dailyBudget, totalBudget);
        this.dailyBudget = dailyBudget;
        this.totalBudget = totalBudget;
        this.spent = spent;
    }

    private void validateBudget(Long dailyBudget, Long totalBudget) {
        if (dailyBudget <= 0 || totalBudget <= 0) {
            throw new IllegalArgumentException("예산은 0보다 커야 합니다");
        }
        if (dailyBudget > totalBudget) {
            throw new IllegalArgumentException("일예산은 총예산을 초과할 수 없습니다");
        }
    }

    /**
     * 지출을 기록하고 새로운 Budget 인스턴스를 반환 (불변성)
     */
    public Budget recordSpent(Long amount) {
        return new Budget(this.dailyBudget, this.totalBudget, this.spent + amount);
    }

    /**
     * 일예산 잔여액
     */
    public Long getRemainingDaily() {
        return dailyBudget - spent;
    }

    /**
     * 총예산 잔여액
     */
    public Long getRemainingTotal() {
        return totalBudget - spent;
    }

    /**
     * 일예산 초과 여부
     */
    public boolean isDailyBudgetExceeded() {
        return spent >= dailyBudget;
    }

    /**
     * 총예산 초과 여부
     */
    public boolean isTotalBudgetExceeded() {
        return spent >= totalBudget;
    }

    /**
     * 예산 초과 여부 (일예산 또는 총예산)
     */
    public boolean isExceeded() {
        return isDailyBudgetExceeded() || isTotalBudgetExceeded();
    }

    /**
     * 특정 금액을 지출할 수 있는지 확인
     */
    public boolean canSpend(Long amount) {
        return (spent + amount) <= dailyBudget && (spent + amount) <= totalBudget;
    }
}
