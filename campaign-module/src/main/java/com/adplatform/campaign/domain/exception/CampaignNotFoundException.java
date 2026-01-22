package com.adplatform.campaign.domain.exception;

/**
 * 캠페인을 찾을 수 없을 때 발생하는 예외
 */
public class CampaignNotFoundException extends RuntimeException {
    public CampaignNotFoundException(String message) {
        super(message);
    }

    public static CampaignNotFoundException withId(String campaignId) {
        return new CampaignNotFoundException("캠페인을 찾을 수 없습니다: " + campaignId);
    }
}
