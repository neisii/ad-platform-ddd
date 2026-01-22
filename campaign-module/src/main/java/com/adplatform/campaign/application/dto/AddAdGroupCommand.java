package com.adplatform.campaign.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 광고그룹 추가 Command DTO
 */
@Getter
@Builder
public class AddAdGroupCommand {
    private final String campaignId;
    private final String name;
    private final Long bid;
}
