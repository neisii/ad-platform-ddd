package com.adplatform.advertiser.domain.exception;

/**
 * 중복된 이메일로 광고주 생성 시 발생하는 예외
 */
public class DuplicateEmailException extends RuntimeException {

    private static final String MESSAGE_FORMAT = "이미 사용중인 이메일입니다: %s";

    public DuplicateEmailException(String message) {
        super(message);
    }

    public static DuplicateEmailException withEmail(String email) {
        return new DuplicateEmailException(String.format(MESSAGE_FORMAT, email));
    }
}
