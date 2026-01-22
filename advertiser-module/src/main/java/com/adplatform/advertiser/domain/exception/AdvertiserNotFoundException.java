package com.adplatform.advertiser.domain.exception;

/**
 * 광고주를 찾을 수 없을 때 발생하는 예외
 */
public class AdvertiserNotFoundException extends RuntimeException {

    private static final String MESSAGE_FORMAT = "광고주를 찾을 수 없습니다. ID: %s";

    public AdvertiserNotFoundException(String message) {
        super(message);
    }

    public static AdvertiserNotFoundException withId(String advertiserId) {
        return new AdvertiserNotFoundException(String.format(MESSAGE_FORMAT, advertiserId));
    }
}
