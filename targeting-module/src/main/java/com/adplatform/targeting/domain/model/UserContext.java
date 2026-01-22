package com.adplatform.targeting.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 컨텍스트 Value Object
 * - 타겟팅 매칭을 위한 사용자 정보
 * - 불변 객체
 */
@Getter
@EqualsAndHashCode
@Builder
public class UserContext {
    private final Integer age;
    private final Gender gender;
    private final String country;
    private final String city;
    private final DeviceType deviceType;
    private final List<String> keywords;

    public UserContext(Integer age, Gender gender, String country, String city,
                       DeviceType deviceType, List<String> keywords) {
        this.age = age;
        this.gender = gender != null ? gender : Gender.ANY;
        this.country = country;
        this.city = city;
        this.deviceType = deviceType;
        this.keywords = keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
    }

    /**
     * 키워드 목록을 불변 리스트로 반환
     */
    public List<String> getKeywords() {
        return new ArrayList<>(keywords);
    }
}
