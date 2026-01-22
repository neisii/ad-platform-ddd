package com.adplatform.campaign.adapter.rest.dto;

import com.adplatform.campaign.domain.model.AdGroup;
import com.adplatform.campaign.domain.model.AdStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * 광고그룹 응답 DTO
 */
@Getter
@Builder
public class AdGroupResponse {
    private final String id;
    private final String campaignId;
    private final String name;
    private final Long bid;
    private final AdStatus status;

    /**
     * Domain Model -> Response DTO 변환
     */
    public static AdGroupResponse from(AdGroup adGroup) {
        return AdGroupResponse.builder()
            .id(adGroup.getId())
            .campaignId(adGroup.getCampaignId())
            .name(adGroup.getName())
            .bid(adGroup.getBid())
            .status(adGroup.getStatus())
            .build();
    }
}
