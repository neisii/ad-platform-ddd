package com.adplatform.targeting.application.dto;

import com.adplatform.targeting.domain.model.DeviceType;
import com.adplatform.targeting.domain.model.Gender;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 타겟팅 매칭 Command
 */
@Getter
@Builder
public class MatchTargetingCommand {
    private final Integer age;
    private final Gender gender;
    private final String country;
    private final String city;
    private final DeviceType deviceType;
    private final List<String> keywords;
}
