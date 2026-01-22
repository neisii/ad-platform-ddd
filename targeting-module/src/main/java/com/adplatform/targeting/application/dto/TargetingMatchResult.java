package com.adplatform.targeting.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 타겟팅 매칭 결과
 */
@Getter
@AllArgsConstructor
public class TargetingMatchResult {
    private final String targetingRuleId;
    private final String campaignId;
    private final int matchScore;

    public static TargetingMatchResult of(String targetingRuleId, String campaignId, int matchScore) {
        return new TargetingMatchResult(targetingRuleId, campaignId, matchScore);
    }
}
