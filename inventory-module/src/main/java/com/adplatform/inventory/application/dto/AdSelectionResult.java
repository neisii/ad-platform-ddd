package com.adplatform.inventory.application.dto;

import com.adplatform.inventory.domain.model.AdSelection;
import lombok.Builder;
import lombok.Getter;

/**
 * 광고 선택 결과 DTO
 */
@Getter
@Builder
public class AdSelectionResult {
    private final String campaignId;
    private final String adGroupId;
    private final String adId;
    private final int matchScore;
    private final long bid;
    private final long estimatedCost;
    private final String impressionToken;

    /**
     * 도메인 모델로부터 결과 생성
     */
    public static AdSelectionResult from(AdSelection adSelection) {
        return AdSelectionResult.builder()
            .campaignId(adSelection.getSelectedAd().getCampaignId())
            .adGroupId(adSelection.getSelectedAd().getAdGroupId())
            .adId(adSelection.getSelectedAd().getAdId())
            .matchScore(adSelection.getMatchScore())
            .bid(adSelection.getBid())
            .estimatedCost(adSelection.getEstimatedCost())
            .impressionToken(adSelection.getImpressionToken())
            .build();
    }
}
