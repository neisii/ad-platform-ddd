package com.adplatform.advertiser.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void 금액을_생성한다() {
        // When
        Money money = Money.of(10000L);

        // Then
        assertThat(money.getAmount()).isEqualTo(10000L);
        assertThat(money.getCurrency()).isEqualTo("KRW");
    }

    @Test
    void 금액은_음수일_수_없다() {
        // When & Then
        assertThatThrownBy(() -> Money.of(-1000L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("금액은 0 이상이어야 합니다");
    }

    @Test
    void 금액을_더한다() {
        // Given
        Money money = Money.of(10000L);

        // When
        Money result = money.add(5000L);

        // Then
        assertThat(result.getAmount()).isEqualTo(15000L);
        assertThat(money.getAmount()).isEqualTo(10000L); // 불변성
    }

    @Test
    void 금액을_뺀다() {
        // Given
        Money money = Money.of(10000L);

        // When
        Money result = money.subtract(3000L);

        // Then
        assertThat(result.getAmount()).isEqualTo(7000L);
    }

    @Test
    void 잔액보다_큰_금액을_뺄_수_없다() {
        // Given
        Money money = Money.of(5000L);

        // When & Then
        assertThatThrownBy(() -> money.subtract(10000L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("차감 후 금액이 음수가 될 수 없습니다");
    }

    @Test
    void 특정_금액을_지불할_수_있는지_확인한다() {
        // Given
        Money money = Money.of(10000L);

        // When & Then
        assertThat(money.canAfford(5000L)).isTrue();
        assertThat(money.canAfford(10000L)).isTrue();
        assertThat(money.canAfford(15000L)).isFalse();
    }

    @Test
    void 불변_객체이므로_연산_시_새_인스턴스를_반환한다() {
        // Given
        Money original = Money.of(10000L);

        // When
        Money added = original.add(5000L);
        Money subtracted = original.subtract(3000L);

        // Then
        assertThat(original.getAmount()).isEqualTo(10000L);
        assertThat(added.getAmount()).isEqualTo(15000L);
        assertThat(subtracted.getAmount()).isEqualTo(7000L);
        assertThat(original).isNotSameAs(added);
        assertThat(original).isNotSameAs(subtracted);
    }
}
