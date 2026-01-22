package com.adplatform.targeting.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

/**
 * 타겟팅 룰 Aggregate Root
 * - 캠페인의 타겟팅 조건 관리
 * - 사용자 컨텍스트와의 매칭 스코어 계산
 */
@Getter
public class TargetingRule {

    private final String id;
    private final String campaignId;
    private Demographics demographics;
    private List<String> geoTargets;
    private List<DeviceType> deviceTypes;
    private List<String> keywords;
    private final Instant createdAt;
    private Instant updatedAt;

    @Builder
    public TargetingRule(
        String id,
        String campaignId,
        Demographics demographics,
        List<String> geoTargets,
        List<DeviceType> deviceTypes,
        List<String> keywords
    ) {
        validateCampaignId(campaignId);
        this.id = id;
        this.campaignId = campaignId;
        this.demographics =
            demographics != null ? demographics : Demographics.any();
        this.geoTargets =
            geoTargets != null
                ? new ArrayList<>(geoTargets)
                : new ArrayList<>();
        this.deviceTypes =
            deviceTypes != null
                ? new ArrayList<>(deviceTypes)
                : new ArrayList<>();
        this.keywords =
            keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private void validateCampaignId(String campaignId) {
        if (campaignId == null || campaignId.trim().isEmpty()) {
            throw new IllegalArgumentException("캠페인 ID는 필수입니다");
        }
    }

    /**
     * 사용자 컨텍스트와의 매칭 스코어 계산 (0-100)
     */
    public int matchScore(UserContext userContext) {
        if (userContext == null) {
            return 0;
        }

        // 모든 조건이 비어있으면 100점 (모든 사용자 타겟)
        if (isEmpty()) {
            return 100;
        }

        int totalScore = 0;
        int maxScore = 0;

        // 1. 인구통계 매칭 (30점) - 조건이 있을 때만 계산
        if (hasDemographicsCriteria()) {
            maxScore += 30;
            if (
                demographics.matches(
                    userContext.getAge(),
                    userContext.getGender()
                )
            ) {
                totalScore += 30;
            }
        }

        // 2. 지역 매칭 (25점) - 조건이 있을 때만 계산
        if (!geoTargets.isEmpty()) {
            maxScore += 25;
            if (matchesGeo(userContext)) {
                totalScore += 25;
            }
        }

        // 3. 디바이스 타입 매칭 (20점) - 조건이 있을 때만 계산
        if (!deviceTypes.isEmpty()) {
            maxScore += 20;
            if (matchesDeviceType(userContext.getDeviceType())) {
                totalScore += 20;
            }
        }

        // 4. 키워드 매칭 (25점) - 조건이 있을 때만 계산
        if (!keywords.isEmpty()) {
            maxScore += 25;
            int keywordScore = calculateKeywordScore(userContext.getKeywords());
            totalScore += keywordScore;
        }

        // 백분율로 정규화
        return maxScore > 0 ? (totalScore * 100) / maxScore : 0;
    }

    /**
     * 인구통계 조건이 있는지 확인
     */
    private boolean hasDemographicsCriteria() {
        return (
            demographics.getAgeMin() != null ||
            demographics.getAgeMax() != null ||
            demographics.getGender() != Gender.ANY
        );
    }

    /**
     * 지역 타겟 매칭
     */
    private boolean matchesGeo(UserContext userContext) {
        if (geoTargets.isEmpty()) {
            return true; // 지역 제한 없음
        }

        String userCountry = userContext.getCountry();
        String userCity = userContext.getCity();

        if (userCountry == null) {
            return false;
        }

        for (String geoTarget : geoTargets) {
            // 국가 매칭
            if (geoTarget.equalsIgnoreCase(userCountry)) {
                return true;
            }
            // 도시 매칭
            if (userCity != null && geoTarget.equalsIgnoreCase(userCity)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 디바이스 타입 매칭
     */
    private boolean matchesDeviceType(DeviceType userDeviceType) {
        if (deviceTypes.isEmpty()) {
            return true; // 디바이스 제한 없음
        }

        if (userDeviceType == null) {
            return false;
        }

        return deviceTypes.contains(userDeviceType);
    }

    /**
     * 키워드 매칭 스코어 계산
     */
    private int calculateKeywordScore(List<String> userKeywords) {
        if (keywords.isEmpty()) {
            return 25; // 키워드 제한 없음
        }

        if (userKeywords == null || userKeywords.isEmpty()) {
            return 0;
        }

        Set<String> targetKeywordsSet = new HashSet<>();
        for (String keyword : keywords) {
            targetKeywordsSet.add(keyword.toLowerCase());
        }

        Set<String> userKeywordsSet = new HashSet<>();
        for (String keyword : userKeywords) {
            userKeywordsSet.add(keyword.toLowerCase());
        }

        // 교집합 계산
        Set<String> intersection = new HashSet<>(targetKeywordsSet);
        intersection.retainAll(userKeywordsSet);

        // 매칭된 키워드 비율로 스코어 계산
        if (!targetKeywordsSet.isEmpty()) {
            return (intersection.size() * 25) / targetKeywordsSet.size();
        }

        return 0;
    }

    /**
     * 모든 타겟팅 조건이 비어있는지 확인
     */
    private boolean isEmpty() {
        return (
            (demographics.getAgeMin() == null &&
                demographics.getAgeMax() == null &&
                demographics.getGender() == Gender.ANY) &&
            geoTargets.isEmpty() &&
            deviceTypes.isEmpty() &&
            keywords.isEmpty()
        );
    }

    /**
     * 타겟팅 룰 업데이트
     */
    public void update(
        Demographics demographics,
        List<String> geoTargets,
        List<DeviceType> deviceTypes,
        List<String> keywords
    ) {
        this.demographics =
            demographics != null ? demographics : Demographics.any();
        this.geoTargets =
            geoTargets != null
                ? new ArrayList<>(geoTargets)
                : new ArrayList<>();
        this.deviceTypes =
            deviceTypes != null
                ? new ArrayList<>(deviceTypes)
                : new ArrayList<>();
        this.keywords =
            keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    /**
     * 불변 리스트 반환
     */
    public List<String> getGeoTargets() {
        return new ArrayList<>(geoTargets);
    }

    public List<DeviceType> getDeviceTypes() {
        return new ArrayList<>(deviceTypes);
    }

    public List<String> getKeywords() {
        return new ArrayList<>(keywords);
    }
}
