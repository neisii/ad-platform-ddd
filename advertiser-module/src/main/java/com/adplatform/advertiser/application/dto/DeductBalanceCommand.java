package com.adplatform.advertiser.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 잔액 차감 Command DTO
 */
@Getter
@Builder
public class DeductBalanceCommand {
    private final String advertiserId;
    private final Long amount;
}
