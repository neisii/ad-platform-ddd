package com.adplatform.billing.infrastructure.persistence;

import com.adplatform.billing.domain.model.BillingTransaction;
import org.springframework.stereotype.Component;

/**
 * BillingTransaction Mapper
 * - Entity <-> Domain Model 변환
 */
@Component
public class BillingTransactionMapper {

    /**
     * Domain Model -> Entity
     */
    public BillingTransactionEntity toEntity(BillingTransaction transaction) {
        return BillingTransactionEntity.builder()
            .id(transaction.getId())
            .advertiserId(transaction.getAdvertiserId())
            .campaignId(transaction.getCampaignId())
            .dailyMetricsId(transaction.getDailyMetricsId())
            .amount(transaction.getAmount())
            .status(transaction.getStatus())
            .billingDate(transaction.getBillingDate())
            .processedAt(transaction.getProcessedAt())
            .description(transaction.getDescription())
            .createdAt(transaction.getCreatedAt())
            .build();
    }

    /**
     * Entity -> Domain Model
     */
    public BillingTransaction toDomain(BillingTransactionEntity entity) {
        return BillingTransaction.builder()
            .id(entity.getId())
            .advertiserId(entity.getAdvertiserId())
            .campaignId(entity.getCampaignId())
            .dailyMetricsId(entity.getDailyMetricsId())
            .amount(entity.getAmount())
            .status(entity.getStatus())
            .billingDate(entity.getBillingDate())
            .processedAt(entity.getProcessedAt())
            .description(entity.getDescription())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
