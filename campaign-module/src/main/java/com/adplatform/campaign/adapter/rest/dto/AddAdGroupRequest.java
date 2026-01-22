package com.adplatform.campaign.adapter.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 광고그룹 추가 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAdGroupRequest {

    @NotBlank(message = "광고그룹 이름은 필수입니다")
    private String name;

    @NotNull(message = "입찰가는 필수입니다")
    @Positive(message = "입찰가는 0보다 커야 합니다")
    private Long bid;
}
