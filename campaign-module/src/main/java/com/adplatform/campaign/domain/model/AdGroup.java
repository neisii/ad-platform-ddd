package com.adplatform.campaign.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 광고그룹 Entity
 * - Campaign에 속함
 * - 여러 Ad를 포함
 * - 입찰가(bid)를 관리
 */
@Getter
public class AdGroup {
    private final String id;
    private final String campaignId;
    private String name;
    private Long bid;
    private AdStatus status;
    private final List<Ad> ads;

    @Builder
    public AdGroup(String id, String campaignId, String name, Long bid, AdStatus status) {
        validateBid(bid);
        this.id = id;
        this.campaignId = campaignId;
        this.name = name;
        this.bid = bid;
        this.status = status != null ? status : AdStatus.ACTIVE;
        this.ads = new ArrayList<>();
    }

    private void validateBid(Long bid) {
        if (bid == null || bid <= 0) {
            throw new IllegalArgumentException("입찰가는 0보다 커야 합니다");
        }
    }

    /**
     * 광고 추가
     */
    public void addAd(Ad ad) {
        if (!ad.getAdGroupId().equals(this.id)) {
            throw new IllegalArgumentException("광고는 현재 광고그룹에 속해야 합니다");
        }
        this.ads.add(ad);
    }

    /**
     * 광고 목록 조회 (불변)
     */
    public List<Ad> getAds() {
        return Collections.unmodifiableList(ads);
    }

    /**
     * 상태 변경
     */
    public void updateStatus(AdStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException("DELETED 상태에서는 상태를 변경할 수 없습니다");
        }
        this.status = newStatus;
    }

    /**
     * 입찰가 업데이트
     */
    public void updateBid(Long newBid) {
        validateBid(newBid);
        this.bid = newBid;
    }

    /**
     * 이름 업데이트
     */
    public void updateName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
        this.name = newName;
    }

    /**
     * 활성화
     */
    public void activate() {
        updateStatus(AdStatus.ACTIVE);
    }

    /**
     * 일시정지
     */
    public void pause() {
        updateStatus(AdStatus.PAUSED);
    }

    /**
     * 삭제
     */
    public void delete() {
        updateStatus(AdStatus.DELETED);
    }

    /**
     * 활성 상태인지 확인
     */
    public boolean isActive() {
        return status.isActive();
    }

    /**
     * 활성 광고가 있는지 확인
     */
    public boolean hasActiveAds() {
        return ads.stream().anyMatch(Ad::isActive);
    }
}
