package com.adplatform.campaign.adapter.rest.dto;

import com.adplatform.campaign.domain.model.AdStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 캠페인 상태 변경 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampaignStatusRequest {

    @NotNull(message = "상태는 필수입니다")
    private AdStatus status;
}
