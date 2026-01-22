package com.adplatform.campaign.infrastructure.client;

/**
 * Advertiser 모듈과 통신하는 클라이언트 인터페이스
 */
public interface AdvertiserClient {

    /**
     * 광고주 존재 여부 확인
     */
    boolean exists(String advertiserId);
}
