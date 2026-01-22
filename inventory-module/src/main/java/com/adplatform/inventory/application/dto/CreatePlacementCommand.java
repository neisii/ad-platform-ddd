package com.adplatform.inventory.application.dto;

import com.adplatform.inventory.domain.model.PlacementType;
import com.adplatform.inventory.domain.model.PricingModel;
import lombok.Builder;
import lombok.Getter;

/**
 * 게재 위치 생성 커맨드
 */
@Getter
@Builder
public class CreatePlacementCommand {
    private final String name;
    private final String publisherId;
    private final PlacementType placementType;
    private final PricingModel pricingModel;
    private final Long basePrice;
}
