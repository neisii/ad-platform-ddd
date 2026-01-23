package com.adplatform.billing.infrastructure.persistence;

import com.adplatform.billing.domain.model.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * BillingTransaction JPA Entity
 */
@Entity
@Table(
    name = "billing_transactions",
    indexes = {
        @Index(name = "idx_advertiser_id", columnList = "advertiser_id"),
        @Index(name = "idx_campaign_id", columnList = "campaign_id"),
        @Index(name = "idx_billing_date", columnList = "billing_date"),
        @Index(name = "idx_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_metrics_id", columnNames = "daily_metrics_id")
    }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingTransactionEntity {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Column(name = "advertiser_id", nullable = false, length = 50)
    private String advertiserId;

    @Column(name = "campaign_id", nullable = false, length = 50)
    private String campaignId;

    @Column(name = "daily_metrics_id", nullable = false, length = 50, unique = true)
    private String dailyMetricsId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "billing_date", nullable = false)
    private LocalDate billingDate;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
