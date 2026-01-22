package com.adplatform.targeting.infrastructure.persistence;

import com.adplatform.targeting.domain.model.Demographics;
import com.adplatform.targeting.domain.model.Gender;
import com.adplatform.targeting.domain.model.TargetingRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Domain Model <-> JPA Entity 변환 Mapper
 */
@Component
public class TargetingRuleMapper {

    /**
     * Domain Model -> JPA Entity
     */
    public TargetingRuleEntity toEntity(TargetingRule targetingRule) {
        Demographics demographics = targetingRule.getDemographics();

        return TargetingRuleEntity.builder()
            .id(targetingRule.getId())
            .campaignId(targetingRule.getCampaignId())
            .ageMin(demographics.getAgeMin())
            .ageMax(demographics.getAgeMax())
            .gender(demographics.getGender())
            .geoTargets(new ArrayList<>(targetingRule.getGeoTargets()))
            .deviceTypes(new ArrayList<>(targetingRule.getDeviceTypes()))
            .keywords(new ArrayList<>(targetingRule.getKeywords()))
            .build();
    }

    /**
     * JPA Entity -> Domain Model
     */
    public TargetingRule toDomain(TargetingRuleEntity entity) {
        Demographics demographics = Demographics.of(
            entity.getAgeMin(),
            entity.getAgeMax(),
            entity.getGender() != null ? entity.getGender() : Gender.ANY
        );

        return TargetingRule.builder()
            .id(entity.getId())
            .campaignId(entity.getCampaignId())
            .demographics(demographics)
            .geoTargets(new ArrayList<>(entity.getGeoTargets()))
            .deviceTypes(new ArrayList<>(entity.getDeviceTypes()))
            .keywords(new ArrayList<>(entity.getKeywords()))
            .build();
    }

    /**
     * 기존 Entity를 Domain 변경사항으로 업데이트
     */
    public void updateEntity(TargetingRuleEntity entity, TargetingRule targetingRule) {
        Demographics demographics = targetingRule.getDemographics();

        entity.updateFromDomain(
            demographics.getAgeMin(),
            demographics.getAgeMax(),
            demographics.getGender(),
            targetingRule.getGeoTargets(),
            targetingRule.getDeviceTypes(),
            targetingRule.getKeywords()
        );
    }
}
