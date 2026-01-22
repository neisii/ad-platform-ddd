package com.adplatform.inventory.infrastructure.persistence;

import com.adplatform.inventory.domain.model.Placement;
import com.adplatform.inventory.domain.model.PlacementStatus;
import com.adplatform.inventory.domain.model.PlacementType;
import com.adplatform.inventory.domain.model.PricingModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 게재 위치 JPA Entity
 */
@Entity
@Table(name = "placements")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlacementEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "publisher_id")
    private String publisherId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "placement_type")
    private PlacementType placementType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "pricing_model")
    private PricingModel pricingModel;

    @Column(nullable = false, name = "base_price")
    private Long basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlacementStatus status;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

    /**
     * 도메인 모델로부터 Entity 생성
     */
    public static PlacementEntity from(Placement placement) {
        return new PlacementEntity(
            placement.getId(),
            placement.getName(),
            placement.getPublisherId(),
            placement.getPlacementType(),
            placement.getPricingModel(),
            placement.getBasePrice(),
            placement.getStatus(),
            placement.getCreatedAt(),
            placement.getUpdatedAt()
        );
    }

    /**
     * Entity를 도메인 모델로 변환
     */
    public Placement toDomain() {
        return Placement.builder()
            .id(id)
            .name(name)
            .publisherId(publisherId)
            .placementType(placementType)
            .pricingModel(pricingModel)
            .basePrice(basePrice)
            .status(status)
            .build();
    }
}
