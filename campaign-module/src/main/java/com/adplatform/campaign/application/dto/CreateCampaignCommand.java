package com.adplatform.campaign.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 캠페인 생성 Command DTO
 */
@Getter
@Builder
public class CreateCampaignCommand {
    private final String advertiserId;
    private final String name;
    private final Long dailyBudget;
    private final Long totalBudget;
    private final LocalDate startDate;
    private final LocalDate endDate;
}
