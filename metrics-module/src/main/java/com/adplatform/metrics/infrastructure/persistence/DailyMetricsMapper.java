package com.adplatform.metrics.infrastructure.persistence;

import com.adplatform.metrics.domain.model.DailyMetrics;
import org.springframework.stereotype.Component;

/**
 * DailyMetrics Mapper
 * - Entity <-> Domain 변환
 */
@Component
public class DailyMetricsMapper {

    /**
     * Entity -> Domain
     */
    public DailyMetrics toDomain(DailyMetricsEntity entity) {
        if (entity == null) {
            return null;
        }

        return DailyMetrics.builderWithTimestamps()
            .id(entity.getId())
            .date(entity.getDate())
            .adId(entity.getAdId())
            .adGroupId(entity.getAdGroupId())
            .campaignId(entity.getCampaignId())
            .impressions(entity.getImpressions())
            .clicks(entity.getClicks())
            .conversions(entity.getConversions())
            .cost(entity.getCost())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Domain -> Entity
     */
    public DailyMetricsEntity toEntity(DailyMetrics domain) {
        if (domain == null) {
            return null;
        }

        return DailyMetricsEntity.builder()
            .id(domain.getId())
            .date(domain.getDate())
            .adId(domain.getAdId())
            .adGroupId(domain.getAdGroupId())
            .campaignId(domain.getCampaignId())
            .impressions(domain.getImpressions())
            .clicks(domain.getClicks())
            .conversions(domain.getConversions())
            .cost(domain.getCost())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }

    /**
     * Domain -> Entity 업데이트
     */
    public void updateEntity(DailyMetricsEntity entity, DailyMetrics domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.updateFromDomain(
            domain.getDate(),
            domain.getAdId(),
            domain.getAdGroupId(),
            domain.getCampaignId(),
            domain.getImpressions(),
            domain.getClicks(),
            domain.getConversions(),
            domain.getCost()
        );
    }
}
