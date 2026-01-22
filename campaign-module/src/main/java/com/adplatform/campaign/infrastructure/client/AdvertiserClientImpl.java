package com.adplatform.campaign.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Advertiser Client 구현체
 * - 프로토타입: Mock 구현
 * - 실제 환경: REST 호출로 대체
 */
@Slf4j
@Component
public class AdvertiserClientImpl implements AdvertiserClient {

    @Override
    public boolean exists(String advertiserId) {
        // TODO: 실제 환경에서는 Advertiser Service에 HTTP 호출
        // RestTemplate 또는 WebClient 사용

        // 프로토타입: 단순 검증
        if (advertiserId == null || advertiserId.trim().isEmpty()) {
            return false;
        }

        // Mock: "adv-"로 시작하는 ID는 존재한다고 가정
        boolean exists = advertiserId.startsWith("adv-");

        log.info("Checking advertiser existence: {} -> {}", advertiserId, exists);

        return exists;
    }
}
