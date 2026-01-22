package com.adplatform.advertiser.adapter.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 잔액 차감 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeductBalanceRequest {

    @NotNull(message = "차감 금액은 필수입니다")
    @Positive(message = "차감 금액은 0보다 커야 합니다")
    private Long amount;
}
