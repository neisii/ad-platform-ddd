package com.adplatform.inventory.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 광고 선택 결과 Value Object
 * - 광고 선택 프로세스의 결과를 나타냄
 * - 불변 객체
 */
@Getter
@ToString
@Builder
public class AdSelection {

    private final SelectedAd selectedAd;
    private final int matchScore;        // 타겟팅 매칭 스코어 (0-100)
    private final long bid;              // 입찰가
    private final long estimatedCost;    // 예상 비용
    private final String impressionToken; // 노출 추적 토큰

    /**
     * 광고 선택 결과 생성
     */
    public static AdSelection of(
        SelectedAd selectedAd,
        int matchScore,
        long bid,
        long estimatedCost,
        String impressionToken
    ) {
        validateMatchScore(matchScore);
        validateBid(bid);
        validateEstimatedCost(estimatedCost);

        if (selectedAd == null) {
            throw new IllegalArgumentException("선택된 광고는 필수입니다");
        }
        if (impressionToken == null || impressionToken.trim().isEmpty()) {
            throw new IllegalArgumentException("노출 토큰은 필수입니다");
        }

        return AdSelection.builder()
            .selectedAd(selectedAd)
            .matchScore(matchScore)
            .bid(bid)
            .estimatedCost(estimatedCost)
            .impressionToken(impressionToken)
            .build();
    }

    private static void validateMatchScore(int matchScore) {
        if (matchScore < 0 || matchScore > 100) {
            throw new IllegalArgumentException("매칭 스코어는 0-100 사이여야 합니다");
        }
    }

    private static void validateBid(long bid) {
        if (bid < 0) {
            throw new IllegalArgumentException("입찰가는 음수일 수 없습니다");
        }
    }

    private static void validateEstimatedCost(long estimatedCost) {
        if (estimatedCost < 0) {
            throw new IllegalArgumentException("예상 비용은 음수일 수 없습니다");
        }
    }

    /**
     * 랭킹 스코어 계산 (입찰가 * 매칭스코어 / 100)
     */
    public long getRankingScore() {
        return (bid * matchScore) / 100;
    }
}
