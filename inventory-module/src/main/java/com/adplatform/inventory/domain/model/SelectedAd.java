package com.adplatform.inventory.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 선택된 광고 Value Object
 * - 광고 참조 정보를 담는 불변 객체
 */
@Getter
@ToString
@Builder
public class SelectedAd {

    private final String campaignId;
    private final String adGroupId;
    private final String adId;

    /**
     * 선택된 광고 생성
     */
    public static SelectedAd of(String campaignId, String adGroupId, String adId) {
        if (campaignId == null || campaignId.trim().isEmpty()) {
            throw new IllegalArgumentException("캠페인 ID는 필수입니다");
        }
        if (adGroupId == null || adGroupId.trim().isEmpty()) {
            throw new IllegalArgumentException("광고 그룹 ID는 필수입니다");
        }
        if (adId == null || adId.trim().isEmpty()) {
            throw new IllegalArgumentException("광고 ID는 필수입니다");
        }

        return SelectedAd.builder()
            .campaignId(campaignId)
            .adGroupId(adGroupId)
            .adId(adId)
            .build();
    }
}
