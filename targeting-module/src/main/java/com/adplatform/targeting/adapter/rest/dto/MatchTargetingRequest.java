package com.adplatform.targeting.adapter.rest.dto;

import com.adplatform.targeting.domain.model.DeviceType;
import com.adplatform.targeting.domain.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 타겟팅 매칭 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchTargetingRequest {

    private Integer age;
    private Gender gender;
    private String country;
    private String city;
    private DeviceType deviceType;
    private List<String> keywords;
}
