package com.adplatform.campaign.application.usecase;

import com.adplatform.campaign.application.dto.UpdateCampaignStatusCommand;
import com.adplatform.campaign.domain.exception.CampaignNotFoundException;
import com.adplatform.campaign.domain.model.AdStatus;
import com.adplatform.campaign.domain.model.Budget;
import com.adplatform.campaign.domain.model.Campaign;
import com.adplatform.campaign.domain.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCampaignStatusUseCaseTest {

    @Mock
    private CampaignRepository campaignRepository;

    private UpdateCampaignStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCampaignStatusUseCase(campaignRepository);
    }

    @Test
    void 캠페인_상태를_변경한다() {
        // Given
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Summer Campaign")
            .budget(new Budget(10000L, 100000L))
            .status(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        when(campaignRepository.findById("camp-1")).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any(Campaign.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCampaignStatusCommand command = UpdateCampaignStatusCommand.builder()
            .campaignId("camp-1")
            .status(AdStatus.PAUSED)
            .build();

        // When
        Campaign updated = useCase.execute(command);

        // Then
        assertThat(updated.getStatus()).isEqualTo(AdStatus.PAUSED);
        verify(campaignRepository).findById("camp-1");
        verify(campaignRepository).save(campaign);
    }

    @Test
    void 존재하지_않는_캠페인은_상태를_변경할_수_없다() {
        // Given
        when(campaignRepository.findById("invalid-camp")).thenReturn(Optional.empty());

        UpdateCampaignStatusCommand command = UpdateCampaignStatusCommand.builder()
            .campaignId("invalid-camp")
            .status(AdStatus.PAUSED)
            .build();

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(CampaignNotFoundException.class)
            .hasMessageContaining("캠페인을 찾을 수 없습니다");

        verify(campaignRepository, never()).save(any());
    }
}
