package com.adplatform.inventory.adapter.rest.dto;

import com.adplatform.inventory.domain.model.Placement;
import com.adplatform.inventory.domain.model.PlacementStatus;
import com.adplatform.inventory.domain.model.PlacementType;
import com.adplatform.inventory.domain.model.PricingModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 게재 위치 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlacementResponse {
    private String id;
    private String name;
    private String publisherId;
    private PlacementType placementType;
    private PricingModel pricingModel;
    private Long basePrice;
    private PlacementStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 도메인 모델로부터 응답 DTO 생성
     */
    public static PlacementResponse from(Placement placement) {
        return PlacementResponse.builder()
            .id(placement.getId())
            .name(placement.getName())
            .publisherId(placement.getPublisherId())
            .placementType(placement.getPlacementType())
            .pricingModel(placement.getPricingModel())
            .basePrice(placement.getBasePrice())
            .status(placement.getStatus())
            .createdAt(placement.getCreatedAt())
            .updatedAt(placement.getUpdatedAt())
            .build();
    }
}
