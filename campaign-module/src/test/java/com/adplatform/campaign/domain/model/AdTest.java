package com.adplatform.campaign.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AdTest {

    @Test
    void 광고를_생성한다() {
        // When
        Ad ad = Ad.builder()
            .id("ad-1")
            .adGroupId("ag-1")
            .title("Summer Sale 50% Off")
            .description("Limited time offer")
            .landingUrl("https://example.com/sale")
            .status(AdStatus.ACTIVE)
            .build();

        // Then
        assertThat(ad.getId()).isEqualTo("ad-1");
        assertThat(ad.getAdGroupId()).isEqualTo("ag-1");
        assertThat(ad.getTitle()).isEqualTo("Summer Sale 50% Off");
        assertThat(ad.getStatus()).isEqualTo(AdStatus.ACTIVE);
    }

    @Test
    void 광고_상태를_변경한다() {
        // Given
        Ad ad = Ad.builder()
            .id("ad-1")
            .adGroupId("ag-1")
            .title("Summer Sale")
            .description("Sale")
            .landingUrl("https://example.com/sale")
            .status(AdStatus.ACTIVE)
            .build();

        // When
        ad.updateStatus(AdStatus.PAUSED);

        // Then
        assertThat(ad.getStatus()).isEqualTo(AdStatus.PAUSED);
    }

    @Test
    void DELETED_상태에서는_상태를_변경할_수_없다() {
        // Given
        Ad ad = Ad.builder()
            .id("ad-1")
            .adGroupId("ag-1")
            .title("Summer Sale")
            .description("Sale")
            .landingUrl("https://example.com/sale")
            .status(AdStatus.DELETED)
            .build();

        // When & Then
        assertThatThrownBy(() -> ad.updateStatus(AdStatus.ACTIVE))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("DELETED 상태에서는 상태를 변경할 수 없습니다");
    }

    @Test
    void 광고_내용을_수정한다() {
        // Given
        Ad ad = Ad.builder()
            .id("ad-1")
            .adGroupId("ag-1")
            .title("Summer Sale")
            .description("Sale")
            .landingUrl("https://example.com/sale")
            .status(AdStatus.ACTIVE)
            .build();

        // When
        ad.updateContent("Winter Sale 70% Off", "Best deal ever", "https://example.com/winter");

        // Then
        assertThat(ad.getTitle()).isEqualTo("Winter Sale 70% Off");
        assertThat(ad.getDescription()).isEqualTo("Best deal ever");
        assertThat(ad.getLandingUrl()).isEqualTo("https://example.com/winter");
    }

    @Test
    void 제목은_필수이다() {
        // When & Then
        assertThatThrownBy(() -> Ad.builder()
            .id("ad-1")
            .adGroupId("ag-1")
            .title(null)
            .description("Sale")
            .landingUrl("https://example.com/sale")
            .status(AdStatus.ACTIVE)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("제목은 필수입니다");
    }

    @Test
    void 랜딩_URL은_유효한_형식이어야_한다() {
        // When & Then
        assertThatThrownBy(() -> Ad.builder()
            .id("ad-1")
            .adGroupId("ag-1")
            .title("Summer Sale")
            .description("Sale")
            .landingUrl("invalid-url")
            .status(AdStatus.ACTIVE)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("유효한 URL 형식이어야 합니다");
    }
}
