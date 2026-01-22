package com.adplatform.advertiser.domain.model;

import com.adplatform.advertiser.domain.exception.InsufficientBalanceException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.regex.Pattern;

/**
 * 광고주 Aggregate Root
 * - 계정 정보 관리
 * - 잔액 관리
 * - 상태 관리
 */
@Getter
public class Advertiser {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final String id;
    private String name;
    private final String email;
    private Money balance;
    private AdvertiserStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    @Builder
    public Advertiser(String id, String name, String email, Money balance,
                      AdvertiserStatus status) {
        validateEmail(email);
        this.id = id;
        this.name = name;
        this.email = email;
        this.balance = balance != null ? balance : Money.of(0L);
        this.status = status != null ? status : AdvertiserStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("유효한 이메일 형식이어야 합니다");
        }
    }

    /**
     * 잔액 충전
     */
    public void chargeBalance(Long amount) {
        this.balance = balance.add(amount);
        this.updatedAt = Instant.now();
    }

    /**
     * 잔액 차감
     */
    public void deductBalance(Long amount) {
        if (!balance.canAfford(amount)) {
            throw InsufficientBalanceException.withAmount(amount, balance.getAmount());
        }
        this.balance = balance.subtract(amount);
        this.updatedAt = Instant.now();
    }

    /**
     * 특정 금액을 지불할 수 있는지 확인
     */
    public boolean canAfford(Long amount) {
        return balance.canAfford(amount);
    }

    /**
     * 광고주 일시정지
     */
    public void suspend() {
        this.status = AdvertiserStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    /**
     * 광고주 활성화
     */
    public void activate() {
        this.status = AdvertiserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * 광고주 삭제
     */
    public void delete() {
        this.status = AdvertiserStatus.DELETED;
        this.updatedAt = Instant.now();
    }

    /**
     * 활성 상태인지 확인
     */
    public boolean isActive() {
        return status == AdvertiserStatus.ACTIVE;
    }

    /**
     * 이름 변경
     */
    public void updateName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
        this.name = newName;
        this.updatedAt = Instant.now();
    }
}
