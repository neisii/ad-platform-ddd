package com.adplatform.targeting.adapter.rest.dto;

import com.adplatform.targeting.application.dto.TargetingMatchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 타겟팅 매칭 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class TargetingMatchResponse {

    private String targetingRuleId;
    private String campaignId;
    private int matchScore;

    public static TargetingMatchResponse from(TargetingMatchResult result) {
        return TargetingMatchResponse.builder()
            .targetingRuleId(result.getTargetingRuleId())
            .campaignId(result.getCampaignId())
            .matchScore(result.getMatchScore())
            .build();
    }
}
