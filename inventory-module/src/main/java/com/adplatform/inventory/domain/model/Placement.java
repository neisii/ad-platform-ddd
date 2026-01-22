package com.adplatform.inventory.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 광고 게재 위치 Aggregate Root
 * - 광고가 표시될 위치와 조건을 관리
 * - 가격 모델과 기본 가격 설정
 */
@Getter
public class Placement {

    private final String id;
    private String name;
    private final String publisherId;
    private PlacementType placementType;
    private PricingModel pricingModel;
    private Long basePrice;  // 기본 가격 (단위: cents/원 등 최소 화폐 단위)
    private PlacementStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    @Builder
    public Placement(
        String id,
        String name,
        String publisherId,
        PlacementType placementType,
        PricingModel pricingModel,
        Long basePrice,
        PlacementStatus status
    ) {
        validatePublisherId(publisherId);
        validateName(name);
        validatePlacementType(placementType);
        validatePricingModel(pricingModel);
        validateBasePrice(basePrice);

        this.id = id;
        this.name = name;
        this.publisherId = publisherId;
        this.placementType = placementType;
        this.pricingModel = pricingModel;
        this.basePrice = basePrice;
        this.status = status != null ? status : PlacementStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private void validatePublisherId(String publisherId) {
        if (publisherId == null || publisherId.trim().isEmpty()) {
            throw new IllegalArgumentException("퍼블리셔 ID는 필수입니다");
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("게재 위치 이름은 필수입니다");
        }
    }

    private void validatePlacementType(PlacementType placementType) {
        if (placementType == null) {
            throw new IllegalArgumentException("게재 위치 타입은 필수입니다");
        }
    }

    private void validatePricingModel(PricingModel pricingModel) {
        if (pricingModel == null) {
            throw new IllegalArgumentException("가격 모델은 필수입니다");
        }
    }

    private void validateBasePrice(Long basePrice) {
        if (basePrice == null || basePrice < 0) {
            throw new IllegalArgumentException("기본 가격은 0 이상이어야 합니다");
        }
    }

    /**
     * 게재 위치 정보 업데이트
     */
    public void update(
        String name,
        PlacementType placementType,
        PricingModel pricingModel,
        Long basePrice
    ) {
        validateName(name);
        validatePlacementType(placementType);
        validatePricingModel(pricingModel);
        validateBasePrice(basePrice);

        this.name = name;
        this.placementType = placementType;
        this.pricingModel = pricingModel;
        this.basePrice = basePrice;
        this.updatedAt = Instant.now();
    }

    /**
     * 게재 위치 활성화
     */
    public void activate() {
        this.status = PlacementStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * 게재 위치 일시정지
     */
    public void pause() {
        this.status = PlacementStatus.PAUSED;
        this.updatedAt = Instant.now();
    }

    /**
     * 게재 위치 삭제
     */
    public void delete() {
        this.status = PlacementStatus.DELETED;
        this.updatedAt = Instant.now();
    }

    /**
     * 활성 상태인지 확인
     */
    public boolean isActive() {
        return status == PlacementStatus.ACTIVE;
    }

    /**
     * 광고 선택 가능 여부 확인
     */
    public boolean canServeAds() {
        return isActive();
    }
}
