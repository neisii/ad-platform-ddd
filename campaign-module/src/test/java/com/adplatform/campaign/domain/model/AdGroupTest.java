package com.adplatform.campaign.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AdGroupTest {

    @Test
    void 광고그룹을_생성한다() {
        // When
        AdGroup adGroup = AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(500L)
            .status(AdStatus.ACTIVE)
            .build();

        // Then
        assertThat(adGroup.getId()).isEqualTo("ag-1");
        assertThat(adGroup.getCampaignId()).isEqualTo("camp-1");
        assertThat(adGroup.getName()).isEqualTo("Product Category A");
        assertThat(adGroup.getBid()).isEqualTo(500L);
        assertThat(adGroup.getStatus()).isEqualTo(AdStatus.ACTIVE);
        assertThat(adGroup.getAds()).isEmpty();
    }

    @Test
    void 입찰가는_양수여야_한다() {
        // When & Then
        assertThatThrownBy(() -> AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(0L)
            .status(AdStatus.ACTIVE)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("입찰가는 0보다 커야 합니다");

        assertThatThrownBy(() -> AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(-100L)
            .status(AdStatus.ACTIVE)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("입찰가는 0보다 커야 합니다");
    }

    @Test
    void 광고를_추가한다() {
        // Given
        AdGroup adGroup = AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(500L)
            .status(AdStatus.ACTIVE)
            .build();

        Ad ad = Ad.builder()
            .id("ad-1")
            .adGroupId("ag-1")
            .title("Summer Sale")
            .description("Sale")
            .landingUrl("https://example.com/sale")
            .status(AdStatus.ACTIVE)
            .build();

        // When
        adGroup.addAd(ad);

        // Then
        assertThat(adGroup.getAds()).hasSize(1);
        assertThat(adGroup.getAds().get(0).getId()).isEqualTo("ad-1");
    }

    @Test
    void 다른_광고그룹의_광고는_추가할_수_없다() {
        // Given
        AdGroup adGroup = AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(500L)
            .status(AdStatus.ACTIVE)
            .build();

        Ad ad = Ad.builder()
            .id("ad-1")
            .adGroupId("ag-2") // 다른 AdGroup
            .title("Summer Sale")
            .description("Sale")
            .landingUrl("https://example.com/sale")
            .status(AdStatus.ACTIVE)
            .build();

        // When & Then
        assertThatThrownBy(() -> adGroup.addAd(ad))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("광고는 현재 광고그룹에 속해야 합니다");
    }

    @Test
    void 광고그룹_상태를_변경한다() {
        // Given
        AdGroup adGroup = AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(500L)
            .status(AdStatus.ACTIVE)
            .build();

        // When
        adGroup.updateStatus(AdStatus.PAUSED);

        // Then
        assertThat(adGroup.getStatus()).isEqualTo(AdStatus.PAUSED);
    }

    @Test
    void 입찰가를_업데이트한다() {
        // Given
        AdGroup adGroup = AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(500L)
            .status(AdStatus.ACTIVE)
            .build();

        // When
        adGroup.updateBid(800L);

        // Then
        assertThat(adGroup.getBid()).isEqualTo(800L);
    }

    @Test
    void 입찰가_업데이트시_음수는_허용하지_않는다() {
        // Given
        AdGroup adGroup = AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(500L)
            .status(AdStatus.ACTIVE)
            .build();

        // When & Then
        assertThatThrownBy(() -> adGroup.updateBid(-100L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("입찰가는 0보다 커야 합니다");
    }

    @Test
    void 활성_광고가_있는지_확인한다() {
        // Given
        AdGroup adGroup = AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(500L)
            .status(AdStatus.ACTIVE)
            .build();

        Ad activeAd = Ad.builder()
            .id("ad-1")
            .adGroupId("ag-1")
            .title("Active Ad")
            .description("Sale")
            .landingUrl("https://example.com/sale")
            .status(AdStatus.ACTIVE)
            .build();

        Ad pausedAd = Ad.builder()
            .id("ad-2")
            .adGroupId("ag-1")
            .title("Paused Ad")
            .description("Sale")
            .landingUrl("https://example.com/sale")
            .status(AdStatus.PAUSED)
            .build();

        // When
        adGroup.addAd(pausedAd);

        // Then
        assertThat(adGroup.hasActiveAds()).isFalse();

        // When
        adGroup.addAd(activeAd);

        // Then
        assertThat(adGroup.hasActiveAds()).isTrue();
    }
}
