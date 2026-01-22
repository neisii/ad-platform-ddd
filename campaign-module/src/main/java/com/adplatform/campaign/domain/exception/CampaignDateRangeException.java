package com.adplatform.campaign.domain.exception;

/**
 * 캠페인 기간 범위 예외
 */
public class CampaignDateRangeException extends RuntimeException {
    public CampaignDateRangeException(String message) {
        super(message);
    }
}
