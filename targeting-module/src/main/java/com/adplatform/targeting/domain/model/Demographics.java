package com.adplatform.targeting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 인구통계 정보 Value Object
 * - 불변 객체
 * - 연령 범위와 성별 정보 포함
 */
@Getter
@EqualsAndHashCode
public class Demographics {
    private final Integer ageMin;
    private final Integer ageMax;
    private final Gender gender;

    private Demographics(Integer ageMin, Integer ageMax, Gender gender) {
        validateAge(ageMin, ageMax);
        this.ageMin = ageMin;
        this.ageMax = ageMax;
        this.gender = gender != null ? gender : Gender.ANY;
    }

    public static Demographics of(Integer ageMin, Integer ageMax, Gender gender) {
        return new Demographics(ageMin, ageMax, gender);
    }

    public static Demographics any() {
        return new Demographics(null, null, Gender.ANY);
    }

    private void validateAge(Integer ageMin, Integer ageMax) {
        if (ageMin != null && ageMin < 0) {
            throw new IllegalArgumentException("최소 연령은 0 이상이어야 합니다");
        }
        if (ageMax != null && ageMax < 0) {
            throw new IllegalArgumentException("최대 연령은 0 이상이어야 합니다");
        }
        if (ageMin != null && ageMax != null && ageMin > ageMax) {
            throw new IllegalArgumentException("최소 연령은 최대 연령보다 클 수 없습니다");
        }
    }

    /**
     * 사용자가 연령 범위에 포함되는지 확인
     */
    public boolean matchesAge(Integer age) {
        if (age == null) {
            return false;
        }
        if (ageMin != null && age < ageMin) {
            return false;
        }
        if (ageMax != null && age > ageMax) {
            return false;
        }
        return true;
    }

    /**
     * 사용자 성별이 타겟과 일치하는지 확인
     */
    public boolean matchesGender(Gender userGender) {
        if (userGender == null) {
            return false;
        }
        return this.gender == Gender.ANY || this.gender == userGender;
    }

    /**
     * 사용자가 인구통계 조건을 만족하는지 확인
     */
    public boolean matches(Integer age, Gender userGender) {
        return matchesAge(age) && matchesGender(userGender);
    }
}
