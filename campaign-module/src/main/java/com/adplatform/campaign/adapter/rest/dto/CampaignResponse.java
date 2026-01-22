package com.adplatform.campaign.adapter.rest.dto;

import com.adplatform.campaign.domain.model.AdStatus;
import com.adplatform.campaign.domain.model.Campaign;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 캠페인 응답 DTO
 */
@Getter
@Builder
public class CampaignResponse {
    private final String id;
    private final String advertiserId;
    private final String name;
    private final BudgetDto budget;
    private final AdStatus status;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Instant createdAt;
    private final Instant updatedAt;

    @Getter
    @Builder
    public static class BudgetDto {
        private final Long dailyBudget;
        private final Long totalBudget;
        private final Long spent;
        private final Long remainingDaily;
        private final Long remainingTotal;
    }

    /**
     * Domain Model -> Response DTO 변환
     */
    public static CampaignResponse from(Campaign campaign) {
        return CampaignResponse.builder()
            .id(campaign.getId())
            .advertiserId(campaign.getAdvertiserId())
            .name(campaign.getName())
            .budget(BudgetDto.builder()
                .dailyBudget(campaign.getBudget().getDailyBudget())
                .totalBudget(campaign.getBudget().getTotalBudget())
                .spent(campaign.getBudget().getSpent())
                .remainingDaily(campaign.getBudget().getRemainingDaily())
                .remainingTotal(campaign.getBudget().getRemainingTotal())
                .build())
            .status(campaign.getStatus())
            .startDate(campaign.getStartDate())
            .endDate(campaign.getEndDate())
            .createdAt(campaign.getCreatedAt())
            .updatedAt(campaign.getUpdatedAt())
            .build();
    }
}
