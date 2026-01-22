package com.adplatform.inventory.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 타겟팅 매칭 결과 DTO (Targeting Service로부터 받음)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetingMatchDto {
    private String campaignId;
    private int matchScore;
    private boolean matched;
}
