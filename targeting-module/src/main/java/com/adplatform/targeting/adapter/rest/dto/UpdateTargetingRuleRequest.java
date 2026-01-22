package com.adplatform.targeting.adapter.rest.dto;

import com.adplatform.targeting.domain.model.DeviceType;
import com.adplatform.targeting.domain.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 타겟팅 룰 업데이트 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTargetingRuleRequest {

    private Integer ageMin;
    private Integer ageMax;
    private Gender gender;
    private List<String> geoTargets;
    private List<DeviceType> deviceTypes;
    private List<String> keywords;
}
