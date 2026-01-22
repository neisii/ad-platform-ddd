package com.adplatform.campaign.domain.model;

import com.adplatform.campaign.domain.exception.CampaignDateRangeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class CampaignTest {

    @Test
    void 캠페인을_생성한다() {
        // When
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Summer Campaign")
            .budget(new Budget(10000L, 100000L))
            .status(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        // Then
        assertThat(campaign.getId()).isEqualTo("camp-1");
        assertThat(campaign.getAdvertiserId()).isEqualTo("adv-1");
        assertThat(campaign.getName()).isEqualTo("Summer Campaign");
        assertThat(campaign.getStatus()).isEqualTo(AdStatus.ACTIVE);
        assertThat(campaign.getAdGroups()).isEmpty();
    }

    @Test
    void 시작일은_종료일보다_이전이어야_한다() {
        // When & Then
        assertThatThrownBy(() -> Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Campaign")
            .budget(new Budget(10000L, 100000L))
            .status(AdStatus.ACTIVE)
            .startDate(LocalDate.now().plusDays(10))
            .endDate(LocalDate.now())
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("시작일은 종료일보다 이전이어야 합니다");
    }

    @Test
    void 예산_초과시_캠페인이_자동_일시정지된다() {
        // Given
        Budget budget = new Budget(10000L, 100000L);
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Summer Campaign")
            .budget(budget)
            .status(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        // When
        campaign.recordSpent(15000L); // 일예산 초과

        // Then
        assertThat(campaign.getStatus()).isEqualTo(AdStatus.PAUSED);
        assertThat(campaign.getBudget().getSpent()).isEqualTo(15000L);
    }

    @Test
    void 캠페인_기간_외에는_활성화할_수_없다() {
        // Given
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Past Campaign")
            .budget(new Budget(10000L, 100000L))
            .status(AdStatus.PAUSED)
            .startDate(LocalDate.now().minusDays(10))
            .endDate(LocalDate.now().minusDays(1)) // 이미 종료
            .build();

        // When & Then
        assertThatThrownBy(() -> campaign.activate())
            .isInstanceOf(CampaignDateRangeException.class)
            .hasMessageContaining("캠페인 기간이 종료되었습니다");
    }

    @Test
    void 캠페인_기간_시작_전에도_활성화할_수_없다() {
        // Given
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Future Campaign")
            .budget(new Budget(10000L, 100000L))
            .status(AdStatus.PAUSED)
            .startDate(LocalDate.now().plusDays(10))
            .endDate(LocalDate.now().plusDays(40))
            .build();

        // When & Then
        assertThatThrownBy(() -> campaign.activate())
            .isInstanceOf(CampaignDateRangeException.class)
            .hasMessageContaining("캠페인 기간이 시작되지 않았습니다");
    }

    @Test
    void 광고그룹을_추가한다() {
        // Given
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Campaign")
            .budget(new Budget(10000L, 100000L))
            .status(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        AdGroup adGroup = AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("AdGroup 1")
            .bid(500L)
            .status(AdStatus.ACTIVE)
            .build();

        // When
        campaign.addAdGroup(adGroup);

        // Then
        assertThat(campaign.getAdGroups()).hasSize(1);
        assertThat(campaign.getAdGroups().get(0).getId()).isEqualTo("ag-1");
    }

    @Test
    void 다른_캠페인의_광고그룹은_추가할_수_없다() {
        // Given
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Campaign")
            .budget(new Budget(10000L, 100000L))
            .status(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        AdGroup adGroup = AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-2") // 다른 Campaign
            .name("AdGroup 1")
            .bid(500L)
            .status(AdStatus.ACTIVE)
            .build();

        // When & Then
        assertThatThrownBy(() -> campaign.addAdGroup(adGroup))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("광고그룹은 현재 캠페인에 속해야 합니다");
    }

    @Test
    void 특정_금액을_지출할_수_있는지_확인한다() {
        // Given
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Campaign")
            .budget(new Budget(10000L, 100000L))
            .status(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        // When & Then
        assertThat(campaign.canSpend(5000L)).isTrue();
        assertThat(campaign.canSpend(15000L)).isFalse();
    }

    @Test
    void 캠페인_기간_내인지_확인한다() {
        // Given
        LocalDate today = LocalDate.now();
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Campaign")
            .budget(new Budget(10000L, 100000L))
            .status(AdStatus.ACTIVE)
            .startDate(today.minusDays(5))
            .endDate(today.plusDays(5))
            .build();

        // When & Then
        assertThat(campaign.isWithinDateRange()).isTrue();
    }

    @Test
    void 캠페인_상태를_변경한다() {
        // Given
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Campaign")
            .budget(new Budget(10000L, 100000L))
            .status(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        // When
        campaign.pause();

        // Then
        assertThat(campaign.getStatus()).isEqualTo(AdStatus.PAUSED);
    }

    @Test
    void 활성_광고그룹이_있는지_확인한다() {
        // Given
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Campaign")
            .budget(new Budget(10000L, 100000L))
            .status(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        AdGroup activeAdGroup = AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("Active AdGroup")
            .bid(500L)
            .status(AdStatus.ACTIVE)
            .build();

        AdGroup pausedAdGroup = AdGroup.builder()
            .id("ag-2")
            .campaignId("camp-1")
            .name("Paused AdGroup")
            .bid(500L)
            .status(AdStatus.PAUSED)
            .build();

        // When
        campaign.addAdGroup(pausedAdGroup);

        // Then
        assertThat(campaign.hasActiveAdGroups()).isFalse();

        // When
        campaign.addAdGroup(activeAdGroup);

        // Then
        assertThat(campaign.hasActiveAdGroups()).isTrue();
    }
}
