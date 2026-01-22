package com.adplatform.inventory.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SelectedAd 값 객체 테스트")
class SelectedAdTest {

    @Test
    @DisplayName("유효한 정보로 SelectedAd를 생성할 수 있다")
    void createSelectedAd_Success() {
        // when
        SelectedAd selectedAd = SelectedAd.of("campaign-1", "adgroup-1", "ad-1");

        // then
        assertThat(selectedAd.getCampaignId()).isEqualTo("campaign-1");
        assertThat(selectedAd.getAdGroupId()).isEqualTo("adgroup-1");
        assertThat(selectedAd.getAdId()).isEqualTo("ad-1");
    }

    @Test
    @DisplayName("캠페인 ID가 null이면 예외가 발생한다")
    void createSelectedAd_NullCampaignId_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            SelectedAd.of(null, "adgroup-1", "ad-1")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("캠페인 ID는 필수입니다");
    }

    @Test
    @DisplayName("광고 그룹 ID가 비어있으면 예외가 발생한다")
    void createSelectedAd_EmptyAdGroupId_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            SelectedAd.of("campaign-1", "", "ad-1")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("광고 그룹 ID는 필수입니다");
    }

    @Test
    @DisplayName("광고 ID가 null이면 예외가 발생한다")
    void createSelectedAd_NullAdId_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
            SelectedAd.of("campaign-1", "adgroup-1", null)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("광고 ID는 필수입니다");
    }
}
