package com.adplatform.billing.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * BillingTransaction 단위 테스트
 */
class BillingTransactionTest {

    @Test
    @DisplayName("거래를 생성할 수 있다")
    void createTransaction() {
        // given
        String dailyMetricsId = "metrics-1";
        String advertiserId = "advertiser-1";
        String campaignId = "campaign-1";
        Long amount = 10000L;
        LocalDate billingDate = LocalDate.of(2024, 1, 15);

        // when
        BillingTransaction transaction = BillingTransaction.builder()
            .id("tx-1")
            .advertiserId(advertiserId)
            .campaignId(campaignId)
            .dailyMetricsId(dailyMetricsId)
            .amount(amount)
            .status(TransactionStatus.PENDING)
            .billingDate(billingDate)
            .description("Test transaction")
            .build();

        // then
        assertThat(transaction.getId()).isEqualTo("tx-1");
        assertThat(transaction.getAdvertiserId()).isEqualTo(advertiserId);
        assertThat(transaction.getCampaignId()).isEqualTo(campaignId);
        assertThat(transaction.getDailyMetricsId()).isEqualTo(dailyMetricsId);
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getBillingDate()).isEqualTo(billingDate);
        assertThat(transaction.getDescription()).isEqualTo("Test transaction");
        assertThat(transaction.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("광고주 ID는 필수다")
    void advertiserIdRequired() {
        // when & then
        assertThatThrownBy(() ->
            BillingTransaction.builder()
                .id("tx-1")
                .advertiserId(null)
                .campaignId("campaign-1")
                .dailyMetricsId("metrics-1")
                .amount(10000L)
                .status(TransactionStatus.PENDING)
                .billingDate(LocalDate.now())
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("광고주 ID는 필수입니다");
    }

    @Test
    @DisplayName("광고주 ID는 공백일 수 없다")
    void advertiserIdCannotBeBlank() {
        // when & then
        assertThatThrownBy(() ->
            BillingTransaction.builder()
                .id("tx-1")
                .advertiserId("  ")
                .campaignId("campaign-1")
                .dailyMetricsId("metrics-1")
                .amount(10000L)
                .status(TransactionStatus.PENDING)
                .billingDate(LocalDate.now())
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("광고주 ID는 필수입니다");
    }

    @Test
    @DisplayName("캠페인 ID는 필수다")
    void campaignIdRequired() {
        // when & then
        assertThatThrownBy(() ->
            BillingTransaction.builder()
                .id("tx-1")
                .advertiserId("advertiser-1")
                .campaignId(null)
                .dailyMetricsId("metrics-1")
                .amount(10000L)
                .status(TransactionStatus.PENDING)
                .billingDate(LocalDate.now())
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("캠페인 ID는 필수입니다");
    }

    @Test
    @DisplayName("일일 메트릭스 ID는 필수다")
    void dailyMetricsIdRequired() {
        // when & then
        assertThatThrownBy(() ->
            BillingTransaction.builder()
                .id("tx-1")
                .advertiserId("advertiser-1")
                .campaignId("campaign-1")
                .dailyMetricsId(null)
                .amount(10000L)
                .status(TransactionStatus.PENDING)
                .billingDate(LocalDate.now())
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("일일 메트릭스 ID는 필수입니다");
    }

    @Test
    @DisplayName("거래 금액은 0보다 커야 한다")
    void amountMustBePositive() {
        // when & then
        assertThatThrownBy(() ->
            BillingTransaction.builder()
                .id("tx-1")
                .advertiserId("advertiser-1")
                .campaignId("campaign-1")
                .dailyMetricsId("metrics-1")
                .amount(0L)
                .status(TransactionStatus.PENDING)
                .billingDate(LocalDate.now())
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("거래 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("거래 금액은 음수일 수 없다")
    void amountCannotBeNegative() {
        // when & then
        assertThatThrownBy(() ->
            BillingTransaction.builder()
                .id("tx-1")
                .advertiserId("advertiser-1")
                .campaignId("campaign-1")
                .dailyMetricsId("metrics-1")
                .amount(-1000L)
                .status(TransactionStatus.PENDING)
                .billingDate(LocalDate.now())
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("거래 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("거래 상태는 필수다")
    void statusRequired() {
        // when & then
        assertThatThrownBy(() ->
            BillingTransaction.builder()
                .id("tx-1")
                .advertiserId("advertiser-1")
                .campaignId("campaign-1")
                .dailyMetricsId("metrics-1")
                .amount(10000L)
                .status(null)
                .billingDate(LocalDate.now())
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("거래 상태는 필수입니다");
    }

    @Test
    @DisplayName("청구 날짜는 필수다")
    void billingDateRequired() {
        // when & then
        assertThatThrownBy(() ->
            BillingTransaction.builder()
                .id("tx-1")
                .advertiserId("advertiser-1")
                .campaignId("campaign-1")
                .dailyMetricsId("metrics-1")
                .amount(10000L)
                .status(TransactionStatus.PENDING)
                .billingDate(null)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("청구 날짜는 필수입니다");
    }

    @Test
    @DisplayName("거래를 완료 상태로 표시할 수 있다")
    void markAsCompleted() {
        // given
        BillingTransaction transaction = createPendingTransaction();

        // when
        BillingTransaction completed = transaction.markAsCompleted();

        // then
        assertThat(completed.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(completed.getProcessedAt()).isNotNull();
        assertThat(completed.getId()).isEqualTo(transaction.getId());
        assertThat(completed.getAmount()).isEqualTo(transaction.getAmount());
    }

    @Test
    @DisplayName("거래를 실패 상태로 표시할 수 있다")
    void markAsFailed() {
        // given
        BillingTransaction transaction = createPendingTransaction();

        // when
        BillingTransaction failed = transaction.markAsFailed();

        // then
        assertThat(failed.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(failed.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("COMPLETED 상태의 거래만 환불할 수 있다")
    void canRefundOnlyCompletedTransaction() {
        // given
        BillingTransaction completed = createPendingTransaction().markAsCompleted();

        // when
        BillingTransaction refunded = completed.refund();

        // then
        assertThat(refunded.getStatus()).isEqualTo(TransactionStatus.REFUNDED);
        assertThat(refunded.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("PENDING 상태의 거래는 환불할 수 없다")
    void cannotRefundPendingTransaction() {
        // given
        BillingTransaction pending = createPendingTransaction();

        // when & then
        assertThatThrownBy(() -> pending.refund())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("COMPLETED 상태의 거래만 환불할 수 있습니다");
    }

    @Test
    @DisplayName("FAILED 상태의 거래는 환불할 수 없다")
    void cannotRefundFailedTransaction() {
        // given
        BillingTransaction failed = createPendingTransaction().markAsFailed();

        // when & then
        assertThatThrownBy(() -> failed.refund())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("COMPLETED 상태의 거래만 환불할 수 있습니다");
    }

    @Test
    @DisplayName("REFUNDED 상태의 거래는 다시 환불할 수 없다")
    void cannotRefundAlreadyRefundedTransaction() {
        // given
        BillingTransaction refunded = createPendingTransaction()
            .markAsCompleted()
            .refund();

        // when & then
        assertThatThrownBy(() -> refunded.refund())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("COMPLETED 상태의 거래만 환불할 수 있습니다");
    }

    @Test
    @DisplayName("동일한 일일 메트릭스 ID를 가진 거래인지 확인할 수 있다")
    void hasSameDailyMetricsId() {
        // given
        BillingTransaction transaction = createPendingTransaction();

        // when & then
        assertThat(transaction.hasDailyMetricsId("metrics-1")).isTrue();
        assertThat(transaction.hasDailyMetricsId("metrics-2")).isFalse();
    }

    // Helper method
    private BillingTransaction createPendingTransaction() {
        return BillingTransaction.builder()
            .id("tx-1")
            .advertiserId("advertiser-1")
            .campaignId("campaign-1")
            .dailyMetricsId("metrics-1")
            .amount(10000L)
            .status(TransactionStatus.PENDING)
            .billingDate(LocalDate.of(2024, 1, 15))
            .description("Test transaction")
            .build();
    }
}
