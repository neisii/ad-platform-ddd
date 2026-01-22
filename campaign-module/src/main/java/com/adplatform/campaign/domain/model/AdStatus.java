package com.adplatform.campaign.domain.model;

/**
 * 광고 상태 Value Object (Enum)
 */
public enum AdStatus {
    /**
     * 활성 - 광고가 송출될 수 있는 상태
     */
    ACTIVE,

    /**
     * 일시정지 - 광고 송출 중단, 재활성화 가능
     */
    PAUSED,

    /**
     * 삭제됨 - 복구 불가능한 상태
     */
    DELETED;

    /**
     * DELETED 상태에서는 다른 상태로 전이할 수 없음
     */
    public boolean canTransitionTo(AdStatus newStatus) {
        if (this == DELETED) {
            return false;
        }
        return true;
    }

    /**
     * 광고 송출 가능한 상태인지 확인
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
}
