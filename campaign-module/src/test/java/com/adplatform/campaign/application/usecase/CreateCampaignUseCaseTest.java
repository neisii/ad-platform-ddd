package com.adplatform.campaign.application.usecase;

import com.adplatform.campaign.application.dto.CreateCampaignCommand;
import com.adplatform.campaign.domain.exception.AdvertiserNotFoundException;
import com.adplatform.campaign.domain.model.AdStatus;
import com.adplatform.campaign.domain.model.Campaign;
import com.adplatform.campaign.domain.repository.CampaignRepository;
import com.adplatform.campaign.infrastructure.client.AdvertiserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCampaignUseCaseTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private AdvertiserClient advertiserClient;

    private CreateCampaignUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCampaignUseCase(campaignRepository, advertiserClient);
    }

    @Test
    void 유효한_광고주로_캠페인을_생성한다() {
        // Given
        when(advertiserClient.exists("adv-1")).thenReturn(true);
        when(campaignRepository.save(any(Campaign.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CreateCampaignCommand command = CreateCampaignCommand.builder()
            .advertiserId("adv-1")
            .name("Summer Sale")
            .dailyBudget(10000L)
            .totalBudget(300000L)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        // When
        Campaign campaign = useCase.execute(command);

        // Then
        assertThat(campaign.getName()).isEqualTo("Summer Sale");
        assertThat(campaign.getAdvertiserId()).isEqualTo("adv-1");
        assertThat(campaign.getStatus()).isEqualTo(AdStatus.ACTIVE);
        assertThat(campaign.getBudget().getDailyBudget()).isEqualTo(10000L);
        assertThat(campaign.getBudget().getTotalBudget()).isEqualTo(300000L);

        verify(advertiserClient).exists("adv-1");
        verify(campaignRepository).save(any(Campaign.class));
    }

    @Test
    void 존재하지_않는_광고주로는_캠페인을_생성할_수_없다() {
        // Given
        when(advertiserClient.exists("invalid-adv")).thenReturn(false);

        CreateCampaignCommand command = CreateCampaignCommand.builder()
            .advertiserId("invalid-adv")
            .name("Campaign")
            .dailyBudget(10000L)
            .totalBudget(300000L)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(AdvertiserNotFoundException.class)
            .hasMessageContaining("광고주를 찾을 수 없습니다");

        verify(campaignRepository, never()).save(any());
    }

    @Test
    void 캠페인_ID가_자동_생성된다() {
        // Given
        when(advertiserClient.exists("adv-1")).thenReturn(true);
        when(campaignRepository.save(any(Campaign.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CreateCampaignCommand command = CreateCampaignCommand.builder()
            .advertiserId("adv-1")
            .name("Summer Sale")
            .dailyBudget(10000L)
            .totalBudget(300000L)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        // When
        Campaign campaign = useCase.execute(command);

        // Then
        assertThat(campaign.getId()).isNotNull();
        assertThat(campaign.getId()).startsWith("camp-");
    }
}
