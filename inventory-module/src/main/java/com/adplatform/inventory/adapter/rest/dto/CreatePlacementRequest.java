package com.adplatform.inventory.adapter.rest.dto;

import com.adplatform.inventory.domain.model.PlacementType;
import com.adplatform.inventory.domain.model.PricingModel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 게재 위치 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlacementRequest {

    @NotBlank(message = "게재 위치 이름은 필수입니다")
    private String name;

    @NotBlank(message = "퍼블리셔 ID는 필수입니다")
    private String publisherId;

    @NotNull(message = "게재 위치 타입은 필수입니다")
    private PlacementType placementType;

    @NotNull(message = "가격 모델은 필수입니다")
    private PricingModel pricingModel;

    @NotNull(message = "기본 가격은 필수입니다")
    @Min(value = 0, message = "기본 가격은 0 이상이어야 합니다")
    private Long basePrice;
}
