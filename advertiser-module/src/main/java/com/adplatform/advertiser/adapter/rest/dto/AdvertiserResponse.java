package com.adplatform.advertiser.adapter.rest.dto;

import com.adplatform.advertiser.domain.model.Advertiser;
import com.adplatform.advertiser.domain.model.AdvertiserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 광고주 응답 DTO
 */
@Getter
@Builder
public class AdvertiserResponse {
    private final String id;
    private final String name;
    private final String email;
    private final MoneyDto balance;
    private final AdvertiserStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    @Getter
    @Builder
    public static class MoneyDto {
        private final Long amount;
        private final String currency;
    }

    /**
     * Domain Model -> Response DTO 변환
     */
    public static AdvertiserResponse from(Advertiser advertiser) {
        return AdvertiserResponse.builder()
            .id(advertiser.getId())
            .name(advertiser.getName())
            .email(advertiser.getEmail())
            .balance(MoneyDto.builder()
                .amount(advertiser.getBalance().getAmount())
                .currency(advertiser.getBalance().getCurrency())
                .build())
            .status(advertiser.getStatus())
            .createdAt(advertiser.getCreatedAt())
            .updatedAt(advertiser.getUpdatedAt())
            .build();
    }
}
