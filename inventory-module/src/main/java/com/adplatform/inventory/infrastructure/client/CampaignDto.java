package com.adplatform.inventory.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 캠페인 정보 DTO (Campaign Service로부터 받음)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDto {
    private String id;
    private String advertiserId;
    private String name;
    private Long dailyBudget;
    private Long bidAmount;
    private String status;
}
