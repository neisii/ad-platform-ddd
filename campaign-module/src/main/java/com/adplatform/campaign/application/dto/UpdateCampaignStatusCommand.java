package com.adplatform.campaign.application.dto;

import com.adplatform.campaign.domain.model.AdStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * 캠페인 상태 변경 Command DTO
 */
@Getter
@Builder
public class UpdateCampaignStatusCommand {
    private final String campaignId;
    private final AdStatus status;
}
