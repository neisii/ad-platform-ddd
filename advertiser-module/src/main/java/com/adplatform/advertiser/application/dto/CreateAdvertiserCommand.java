package com.adplatform.advertiser.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 광고주 생성 Command DTO
 */
@Getter
@Builder
public class CreateAdvertiserCommand {
    private final String name;
    private final String email;
}
