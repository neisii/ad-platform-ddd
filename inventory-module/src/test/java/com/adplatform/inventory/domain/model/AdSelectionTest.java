package com.adplatform.inventory.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AdSelection 값 객체 테스트")
class AdSelectionTest {

    @Test
    @DisplayName("유효한 정보로 AdSelection을 생성할 수 있다")
    void createAdSelection_Success() {
        // given
        SelectedAd selectedAd = SelectedAd.of("campaign-1", "adgroup-1", "ad-1");

        // when
        AdSelection adSelection = AdSelection.of(
            selectedAd,
            85,
            5000L,
            250L,
            "impression-token-123"
        );

        // then
        assertThat(adSelection.getSelectedAd()).isEqualTo(selectedAd);
        assertThat(adSelection.getMatchScore()).isEqualTo(85);
        assertThat(adSelection.getBid()).isEqualTo(5000L);
        assertThat(adSelection.getEstimatedCost()).isEqualTo(250L);
        assertThat(adSelection.getImpressionToken()).isEqualTo("impression-token-123");
    }

    @Test
    @DisplayName("랭킹 스코어를 올바르게 계산한다")
    void calculateRankingScore_Success() {
        // given
        SelectedAd selectedAd = SelectedAd.of("campaign-1", "adgroup-1", "ad-1");
        AdSelection adSelection = AdSelection.of(
            selectedAd,
            80,      // matchScore
            10000L,  // bid
            500L,
            "token"
        );

        // when
        long rankingScore = adSelection.getRankingScore();

        // then
        // 10000 * 80 / 100 = 8000
        assertThat(rankingScore).isEqualTo(8000L);
    }

    @Test
    @DisplayName("매칭 스코어가 0보다 작으면 예외가 발생한다")
    void createAdSelection_NegativeMatchScore_ThrowsException() {
        // given
        SelectedAd selectedAd = SelectedAd.of("campaign-1", "adgroup-1", "ad-1");

        // when & then
        assertThatThrownBy(() ->
            AdSelection.of(selectedAd, -1, 5000L, 250L, "token")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("매칭 스코어는 0-100 사이여야 합니다");
    }

    @Test
    @DisplayName("매칭 스코어가 100보다 크면 예외가 발생한다")
    void createAdSelection_MatchScoreTooHigh_ThrowsException() {
        // given
        SelectedAd selectedAd = SelectedAd.of("campaign-1", "adgroup-1", "ad-1");

        // when & then
        assertThatThrownBy(() ->
            AdSelection.of(selectedAd, 101, 5000L, 250L, "token")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("매칭 스코어는 0-100 사이여야 합니다");
    }

    @Test
    @DisplayName("입찰가가 음수면 예외가 발생한다")
    void createAdSelection_NegativeBid_ThrowsException() {
        // given
        SelectedAd selectedAd = SelectedAd.of("campaign-1", "adgroup-1", "ad-1");

        // when & then
        assertThatThrownBy(() ->
            AdSelection.of(selectedAd, 85, -1L, 250L, "token")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("입찰가는 음수일 수 없습니다");
    }

    @Test
    @DisplayName("선택된 광고가 null이면 예외가 발생한다")
    void createAdSelection_NullSelectedAd_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            AdSelection.of(null, 85, 5000L, 250L, "token")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("선택된 광고는 필수입니다");
    }

    @Test
    @DisplayName("노출 토큰이 비어있으면 예외가 발생한다")
    void createAdSelection_EmptyToken_ThrowsException() {
        // given
        SelectedAd selectedAd = SelectedAd.of("campaign-1", "adgroup-1", "ad-1");

        // when & then
        assertThatThrownBy(() ->
            AdSelection.of(selectedAd, 85, 5000L, 250L, "")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("노출 토큰은 필수입니다");
    }
}
