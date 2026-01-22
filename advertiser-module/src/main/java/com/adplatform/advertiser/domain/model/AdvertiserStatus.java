package com.adplatform.advertiser.domain.model;

/**
 * 광고주 상태
 */
public enum AdvertiserStatus {
    /**
     * 활성 - 광고 집행 가능
     */
    ACTIVE,

    /**
     * 일시정지 - 광고 집행 불가
     */
    SUSPENDED,

    /**
     * 삭제됨 - 복구 불가
     */
    DELETED
}
