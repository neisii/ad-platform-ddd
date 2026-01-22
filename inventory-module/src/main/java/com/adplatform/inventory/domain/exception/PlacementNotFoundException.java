package com.adplatform.inventory.domain.exception;

/**
 * 게재 위치를 찾을 수 없을 때 발생하는 예외
 */
public class PlacementNotFoundException extends RuntimeException {

    public PlacementNotFoundException(String placementId) {
        super("게재 위치를 찾을 수 없습니다: " + placementId);
    }

    public static PlacementNotFoundException withId(String placementId) {
        return new PlacementNotFoundException(placementId);
    }
}
