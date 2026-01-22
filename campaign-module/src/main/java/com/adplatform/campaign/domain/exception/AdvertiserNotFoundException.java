package com.adplatform.campaign.domain.exception;

/**
 * 광고주를 찾을 수 없을 때 발생하는 예외
 */
public class AdvertiserNotFoundException extends RuntimeException {
    public AdvertiserNotFoundException(String message) {
        super(message);
    }

    public static AdvertiserNotFoundException withId(String advertiserId) {
        return new AdvertiserNotFoundException("광고주를 찾을 수 없습니다: " + advertiserId);
    }
}
