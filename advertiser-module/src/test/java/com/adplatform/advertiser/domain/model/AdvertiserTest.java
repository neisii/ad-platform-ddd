package com.adplatform.advertiser.domain.model;

import com.adplatform.advertiser.domain.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AdvertiserTest {

    @Test
    void 광고주를_생성한다() {
        // When
        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Company")
            .email("test@example.com")
            .balance(Money.of(100000L))
            .build();

        // Then
        assertThat(advertiser.getId()).isEqualTo("adv-1");
        assertThat(advertiser.getName()).isEqualTo("Test Company");
        assertThat(advertiser.getEmail()).isEqualTo("test@example.com");
        assertThat(advertiser.getBalance().getAmount()).isEqualTo(100000L);
        assertThat(advertiser.getStatus()).isEqualTo(AdvertiserStatus.ACTIVE);
    }

    @Test
    void 이메일_형식이_유효하지_않으면_생성할_수_없다() {
        // When & Then
        assertThatThrownBy(() -> Advertiser.builder()
            .id("adv-1")
            .name("Test Company")
            .email("invalid-email")
            .balance(Money.of(100000L))
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("유효한 이메일 형식이어야 합니다");
    }

    @Test
    void 잔액을_충전한다() {
        // Given
        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Company")
            .email("test@example.com")
            .balance(Money.of(100000L))
            .build();

        // When
        advertiser.chargeBalance(50000L);

        // Then
        assertThat(advertiser.getBalance().getAmount()).isEqualTo(150000L);
    }

    @Test
    void 잔액을_차감한다() {
        // Given
        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Company")
            .email("test@example.com")
            .balance(Money.of(100000L))
            .build();

        // When
        advertiser.deductBalance(30000L);

        // Then
        assertThat(advertiser.getBalance().getAmount()).isEqualTo(70000L);
    }

    @Test
    void 잔액보다_큰_금액을_차감할_수_없다() {
        // Given
        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Company")
            .email("test@example.com")
            .balance(Money.of(100000L))
            .build();

        // When & Then
        assertThatThrownBy(() -> advertiser.deductBalance(150000L))
            .isInstanceOf(InsufficientBalanceException.class)
            .hasMessageContaining("잔액이 부족합니다");
    }

    @Test
    void 특정_금액을_지불할_수_있는지_확인한다() {
        // Given
        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Company")
            .email("test@example.com")
            .balance(Money.of(100000L))
            .build();

        // When & Then
        assertThat(advertiser.canAfford(50000L)).isTrue();
        assertThat(advertiser.canAfford(100000L)).isTrue();
        assertThat(advertiser.canAfford(150000L)).isFalse();
    }

    @Test
    void 광고주를_일시정지한다() {
        // Given
        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Company")
            .email("test@example.com")
            .balance(Money.of(100000L))
            .build();

        // When
        advertiser.suspend();

        // Then
        assertThat(advertiser.getStatus()).isEqualTo(AdvertiserStatus.SUSPENDED);
    }

    @Test
    void 광고주를_활성화한다() {
        // Given
        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Company")
            .email("test@example.com")
            .balance(Money.of(100000L))
            .status(AdvertiserStatus.SUSPENDED)
            .build();

        // When
        advertiser.activate();

        // Then
        assertThat(advertiser.getStatus()).isEqualTo(AdvertiserStatus.ACTIVE);
    }

    @Test
    void 활성_상태인지_확인한다() {
        // Given
        Advertiser active = Advertiser.builder()
            .id("adv-1")
            .name("Test Company")
            .email("test@example.com")
            .balance(Money.of(100000L))
            .build();

        Advertiser suspended = Advertiser.builder()
            .id("adv-2")
            .name("Test Company 2")
            .email("test2@example.com")
            .balance(Money.of(100000L))
            .status(AdvertiserStatus.SUSPENDED)
            .build();

        // When & Then
        assertThat(active.isActive()).isTrue();
        assertThat(suspended.isActive()).isFalse();
    }
}
