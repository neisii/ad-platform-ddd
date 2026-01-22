package com.adplatform.inventory.domain.exception;

/**
 * 비활성 게재 위치에 광고 요청이 왔을 때 발생하는 예외
 */
public class InactivePlacementException extends RuntimeException {

    public InactivePlacementException(String placementId, String status) {
        super("게재 위치가 활성 상태가 아닙니다 (ID: " + placementId + ", 상태: " + status + ")");
    }

    public static InactivePlacementException withStatus(String placementId, String status) {
        return new InactivePlacementException(placementId, status);
    }
}
