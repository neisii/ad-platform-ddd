package com.adplatform.inventory.adapter.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 광고 선택 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectAdRequest {

    @NotBlank(message = "게재 위치 ID는 필수입니다")
    private String placementId;

    @NotNull(message = "사용자 컨텍스트는 필수입니다")
    private UserContext userContext;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserContext {
        private String userId;
        private Integer age;
        private String gender;
        private String country;
        private String city;
        private String deviceType;
        private List<String> keywords;
    }
}
