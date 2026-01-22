package com.adplatform.inventory.adapter.rest.dto;

import com.adplatform.inventory.application.dto.AdSelectionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 광고 선택 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdSelectionResponse {
    private String campaignId;
    private String adGroupId;
    private String adId;
    private int matchScore;
    private long bid;
    private long estimatedCost;
    private String impressionToken;

    /**
     * AdSelectionResult로부터 응답 생성
     */
    public static AdSelectionResponse from(AdSelectionResult result) {
        return AdSelectionResponse.builder()
            .campaignId(result.getCampaignId())
            .adGroupId(result.getAdGroupId())
            .adId(result.getAdId())
            .matchScore(result.getMatchScore())
            .bid(result.getBid())
            .estimatedCost(result.getEstimatedCost())
            .impressionToken(result.getImpressionToken())
            .build();
    }
}
