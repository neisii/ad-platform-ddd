package com.adplatform.targeting.application.dto;

import com.adplatform.targeting.domain.model.DeviceType;
import com.adplatform.targeting.domain.model.Gender;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 타겟팅 룰 업데이트 Command
 */
@Getter
@Builder
public class UpdateTargetingRuleCommand {
    private final String targetingRuleId;
    private final Integer ageMin;
    private final Integer ageMax;
    private final Gender gender;
    private final List<String> geoTargets;
    private final List<DeviceType> deviceTypes;
    private final List<String> keywords;
}
