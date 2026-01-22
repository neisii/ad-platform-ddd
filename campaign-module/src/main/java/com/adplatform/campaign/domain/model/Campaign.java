package com.adplatform.campaign.domain.model;

import com.adplatform.campaign.domain.exception.CampaignDateRangeException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 캠페인 Aggregate Root
 * - 광고주의 광고 캠페인
 * - 여러 AdGroup을 포함
 * - 예산과 기간을 관리
 */
@Getter
public class Campaign {
    private final String id;
    private final String advertiserId;
    private String name;
    private Budget budget;
    private AdStatus status;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<AdGroup> adGroups;
    private final Instant createdAt;
    private Instant updatedAt;

    @Builder
    public Campaign(String id, String advertiserId, String name, Budget budget,
                    AdStatus status, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        this.id = id;
        this.advertiserId = advertiserId;
        this.name = name;
        this.budget = budget;
        this.status = status != null ? status : AdStatus.ACTIVE;
        this.startDate = startDate;
        this.endDate = endDate;
        this.adGroups = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다");
        }
    }

    /**
     * 광고그룹 추가
     */
    public void addAdGroup(AdGroup adGroup) {
        if (!adGroup.getCampaignId().equals(this.id)) {
            throw new IllegalArgumentException("광고그룹은 현재 캠페인에 속해야 합니다");
        }
        this.adGroups.add(adGroup);
        this.updatedAt = Instant.now();
    }

    /**
     * 광고그룹 목록 조회 (불변)
     */
    public List<AdGroup> getAdGroups() {
        return Collections.unmodifiableList(adGroups);
    }

    /**
     * 지출 기록
     * - 예산 초과 시 자동으로 PAUSED 상태로 전환
     */
    public void recordSpent(Long amount) {
        this.budget = budget.recordSpent(amount);

        if (budget.isExceeded()) {
            this.status = AdStatus.PAUSED;
        }

        this.updatedAt = Instant.now();
    }

    /**
     * 특정 금액을 지출할 수 있는지 확인
     */
    public boolean canSpend(Long amount) {
        return budget.canSpend(amount);
    }

    /**
     * 캠페인 활성화
     * - 캠페인 기간 내에만 활성화 가능
     */
    public void activate() {
        if (!isWithinDateRange()) {
            LocalDate today = LocalDate.now();
            if (today.isBefore(startDate)) {
                throw new CampaignDateRangeException("캠페인 기간이 시작되지 않았습니다");
            } else {
                throw new CampaignDateRangeException("캠페인 기간이 종료되었습니다");
            }
        }

        updateStatus(AdStatus.ACTIVE);
    }

    /**
     * 캠페인 일시정지
     */
    public void pause() {
        updateStatus(AdStatus.PAUSED);
    }

    /**
     * 캠페인 삭제
     */
    public void delete() {
        updateStatus(AdStatus.DELETED);
    }

    /**
     * 상태 변경
     */
    public void updateStatus(AdStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException("DELETED 상태에서는 상태를 변경할 수 없습니다");
        }
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    /**
     * 캠페인 이름 변경
     */
    public void updateName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
        this.name = newName;
        this.updatedAt = Instant.now();
    }

    /**
     * 예산 업데이트
     */
    public void updateBudget(Budget newBudget) {
        this.budget = newBudget;
        this.updatedAt = Instant.now();
    }

    /**
     * 캠페인 기간 내인지 확인
     */
    public boolean isWithinDateRange() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * 활성 상태인지 확인
     */
    public boolean isActive() {
        return status.isActive() && isWithinDateRange() && !budget.isExceeded();
    }

    /**
     * 활성 광고그룹이 있는지 확인
     */
    public boolean hasActiveAdGroups() {
        return adGroups.stream().anyMatch(AdGroup::isActive);
    }
}
