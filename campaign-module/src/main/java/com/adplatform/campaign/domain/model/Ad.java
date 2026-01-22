package com.adplatform.campaign.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * 광고 Entity
 * - AdGroup에 속함
 * - 실제 사용자에게 노출되는 광고 크리에이티브
 */
@Getter
public class Ad {
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$"
    );

    private final String id;
    private final String adGroupId;
    private String title;
    private String description;
    private String landingUrl;
    private AdStatus status;

    @Builder
    public Ad(String id, String adGroupId, String title, String description,
              String landingUrl, AdStatus status) {
        validateAd(title, landingUrl);
        this.id = id;
        this.adGroupId = adGroupId;
        this.title = title;
        this.description = description;
        this.landingUrl = landingUrl;
        this.status = status != null ? status : AdStatus.ACTIVE;
    }

    private void validateAd(String title, String landingUrl) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다");
        }
        if (landingUrl == null || !URL_PATTERN.matcher(landingUrl).matches()) {
            throw new IllegalArgumentException("유효한 URL 형식이어야 합니다");
        }
    }

    /**
     * 광고 상태 변경
     */
    public void updateStatus(AdStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException("DELETED 상태에서는 상태를 변경할 수 없습니다");
        }
        this.status = newStatus;
    }

    /**
     * 광고 내용 수정
     */
    public void updateContent(String title, String description, String landingUrl) {
        validateAd(title, landingUrl);
        this.title = title;
        this.description = description;
        this.landingUrl = landingUrl;
    }

    /**
     * 광고 활성화
     */
    public void activate() {
        updateStatus(AdStatus.ACTIVE);
    }

    /**
     * 광고 일시정지
     */
    public void pause() {
        updateStatus(AdStatus.PAUSED);
    }

    /**
     * 광고 삭제
     */
    public void delete() {
        updateStatus(AdStatus.DELETED);
    }

    /**
     * 광고가 활성 상태인지 확인
     */
    public boolean isActive() {
        return status.isActive();
    }
}
