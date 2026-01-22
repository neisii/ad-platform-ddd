package com.adplatform.advertiser.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 잔액 충전 Command DTO
 */
@Getter
@Builder
public class ChargeBalanceCommand {
    private final String advertiserId;
    private final Long amount;
}
