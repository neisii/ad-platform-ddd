package com.adplatform.inventory.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 광고 선택 커맨드
 */
@Getter
@Builder
public class SelectAdCommand {
    private final String placementId;
    private final UserContext userContext;

    @Getter
    @Builder
    public static class UserContext {
        private final String userId;
        private final Integer age;
        private final String gender;
        private final String country;
        private final String city;
        private final String deviceType;
        private final List<String> keywords;
    }
}
