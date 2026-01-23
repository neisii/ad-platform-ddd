package com.adplatform.billing.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * TransactionStatus 단위 테스트
 */
class TransactionStatusTest {

    @Test
    @DisplayName("PENDING 상태는 재시도 가능하다")
    void pendingCanBeRetried() {
        // given
        TransactionStatus status = TransactionStatus.PENDING;

        // when & then
        assertThat(status.canRetry()).isTrue();
    }

    @Test
    @DisplayName("COMPLETED 상태는 재시도 불가능하다")
    void completedCannotBeRetried() {
        // given
        TransactionStatus status = TransactionStatus.COMPLETED;

        // when & then
        assertThat(status.canRetry()).isFalse();
    }

    @Test
    @DisplayName("FAILED 상태는 재시도 가능하다")
    void failedCanBeRetried() {
        // given
        TransactionStatus status = TransactionStatus.FAILED;

        // when & then
        assertThat(status.canRetry()).isTrue();
    }

    @Test
    @DisplayName("REFUNDED 상태는 재시도 불가능하다")
    void refundedCannotBeRetried() {
        // given
        TransactionStatus status = TransactionStatus.REFUNDED;

        // when & then
        assertThat(status.canRetry()).isFalse();
    }

    @Test
    @DisplayName("COMPLETED 상태는 환불 가능하다")
    void completedCanBeRefunded() {
        // given
        TransactionStatus status = TransactionStatus.COMPLETED;

        // when & then
        assertThat(status.canRefund()).isTrue();
    }

    @Test
    @DisplayName("PENDING 상태는 환불 불가능하다")
    void pendingCannotBeRefunded() {
        // given
        TransactionStatus status = TransactionStatus.PENDING;

        // when & then
        assertThat(status.canRefund()).isFalse();
    }

    @Test
    @DisplayName("FAILED 상태는 환불 불가능하다")
    void failedCannotBeRefunded() {
        // given
        TransactionStatus status = TransactionStatus.FAILED;

        // when & then
        assertThat(status.canRefund()).isFalse();
    }

    @Test
    @DisplayName("REFUNDED 상태는 환불 불가능하다")
    void refundedCannotBeRefunded() {
        // given
        TransactionStatus status = TransactionStatus.REFUNDED;

        // when & then
        assertThat(status.canRefund()).isFalse();
    }
}
