package com.adplatform.campaign.adapter.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 캠페인 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampaignRequest {

    @NotBlank(message = "광고주 ID는 필수입니다")
    private String advertiserId;

    @NotBlank(message = "캠페인 이름은 필수입니다")
    private String name;

    @NotNull(message = "일예산은 필수입니다")
    @Positive(message = "일예산은 0보다 커야 합니다")
    private Long dailyBudget;

    @NotNull(message = "총예산은 필수입니다")
    @Positive(message = "총예산은 0보다 커야 합니다")
    private Long totalBudget;

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;
}
