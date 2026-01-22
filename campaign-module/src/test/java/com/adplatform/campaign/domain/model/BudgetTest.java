package com.adplatform.campaign.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BudgetTest {

    @Test
    void 예산을_생성한다() {
        // When
        Budget budget = new Budget(10000L, 100000L);

        // Then
        assertThat(budget.getDailyBudget()).isEqualTo(10000L);
        assertThat(budget.getTotalBudget()).isEqualTo(100000L);
        assertThat(budget.getSpent()).isEqualTo(0L);
    }

    @Test
    void 일예산은_총예산을_초과할_수_없다() {
        // When & Then
        assertThatThrownBy(() -> new Budget(100000L, 50000L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("일예산은 총예산을 초과할 수 없습니다");
    }

    @Test
    void 예산은_음수일_수_없다() {
        // When & Then
        assertThatThrownBy(() -> new Budget(-1000L, 10000L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("예산은 0보다 커야 합니다");

        assertThatThrownBy(() -> new Budget(10000L, -1000L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("예산은 0보다 커야 합니다");
    }

    @Test
    void 지출_기록_후_잔여_예산이_계산된다() {
        // Given
        Budget budget = new Budget(10000L, 100000L);

        // When
        Budget updated = budget.recordSpent(3000L);

        // Then
        assertThat(updated.getSpent()).isEqualTo(3000L);
        assertThat(updated.getRemainingDaily()).isEqualTo(7000L);
        assertThat(updated.getRemainingTotal()).isEqualTo(97000L);
    }

    @Test
    void 일예산_초과_여부를_판단한다() {
        // Given
        Budget budget = new Budget(10000L, 100000L);
        Budget withSpent = budget.recordSpent(9500L);

        // When & Then
        assertThat(withSpent.isDailyBudgetExceeded()).isFalse();
        assertThat(withSpent.canSpend(600L)).isFalse(); // 10000 초과
        assertThat(withSpent.canSpend(400L)).isTrue();
    }

    @Test
    void 총예산_초과_여부를_판단한다() {
        // Given
        Budget budget = new Budget(50000L, 100000L);
        Budget withSpent = budget.recordSpent(99500L);

        // When & Then
        assertThat(withSpent.isTotalBudgetExceeded()).isFalse();
        assertThat(withSpent.canSpend(600L)).isFalse(); // 100000 초과
        assertThat(withSpent.canSpend(400L)).isFalse(); // 일예산도 초과
    }

    @Test
    void 예산이_초과되었는지_확인한다() {
        // Given
        Budget budget = new Budget(10000L, 100000L);

        // When
        Budget dailyExceeded = budget.recordSpent(15000L);
        Budget totalExceeded = budget.recordSpent(105000L);

        // Then
        assertThat(dailyExceeded.isExceeded()).isTrue();
        assertThat(totalExceeded.isExceeded()).isTrue();
    }

    @Test
    void 불변_객체이므로_지출_기록_시_새_인스턴스를_반환한다() {
        // Given
        Budget original = new Budget(10000L, 100000L);

        // When
        Budget updated = original.recordSpent(3000L);

        // Then
        assertThat(original.getSpent()).isEqualTo(0L);
        assertThat(updated.getSpent()).isEqualTo(3000L);
        assertThat(original).isNotSameAs(updated);
    }
}
