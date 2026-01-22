package com.adplatform.targeting.domain.exception;

/**
 * 타겟팅 룰을 찾을 수 없을 때 발생하는 예외
 */
public class TargetingRuleNotFoundException extends RuntimeException {

    private final String targetingRuleId;

    private TargetingRuleNotFoundException(String targetingRuleId, String message) {
        super(message);
        this.targetingRuleId = targetingRuleId;
    }

    public static TargetingRuleNotFoundException withId(String id) {
        return new TargetingRuleNotFoundException(
            id,
            String.format("타겟팅 룰을 찾을 수 없습니다: %s", id)
        );
    }

    public String getTargetingRuleId() {
        return targetingRuleId;
    }
}
