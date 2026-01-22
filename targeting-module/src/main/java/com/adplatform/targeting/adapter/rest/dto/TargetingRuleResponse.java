package com.adplatform.targeting.adapter.rest.dto;

import com.adplatform.targeting.domain.model.DeviceType;
import com.adplatform.targeting.domain.model.Gender;
import com.adplatform.targeting.domain.model.TargetingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * 타겟팅 룰 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class TargetingRuleResponse {

    private String id;
    private String campaignId;
    private Integer ageMin;
    private Integer ageMax;
    private Gender gender;
    private List<String> geoTargets;
    private List<DeviceType> deviceTypes;
    private List<String> keywords;
    private Instant createdAt;
    private Instant updatedAt;

    public static TargetingRuleResponse from(TargetingRule targetingRule) {
        return TargetingRuleResponse.builder()
            .id(targetingRule.getId())
            .campaignId(targetingRule.getCampaignId())
            .ageMin(targetingRule.getDemographics().getAgeMin())
            .ageMax(targetingRule.getDemographics().getAgeMax())
            .gender(targetingRule.getDemographics().getGender())
            .geoTargets(targetingRule.getGeoTargets())
            .deviceTypes(targetingRule.getDeviceTypes())
            .keywords(targetingRule.getKeywords())
            .createdAt(targetingRule.getCreatedAt())
            .updatedAt(targetingRule.getUpdatedAt())
            .build();
    }
}
