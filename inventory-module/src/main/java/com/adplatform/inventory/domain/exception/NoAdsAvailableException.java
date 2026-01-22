package com.adplatform.inventory.domain.exception;

/**
 * 선택 가능한 광고가 없을 때 발생하는 예외
 */
public class NoAdsAvailableException extends RuntimeException {

    public NoAdsAvailableException(String placementId) {
        super("선택 가능한 광고가 없습니다 (게재위치: " + placementId + ")");
    }

    public static NoAdsAvailableException forPlacement(String placementId) {
        return new NoAdsAvailableException(placementId);
    }
}
