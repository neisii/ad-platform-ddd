package com.adplatform.campaign.application.usecase;

import com.adplatform.campaign.application.dto.AddAdGroupCommand;
import com.adplatform.campaign.domain.exception.CampaignNotFoundException;
import com.adplatform.campaign.domain.model.AdGroup;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddAdGroupUseCaseTest {

    @Mock
    private CampaignRepository campaignRepository;

    private AddAdGroupUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddAdGroupUseCase(campaignRepository);
    }

    @Test
    void 캠페인에_광고그룹을_추가한다() {
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

        AddAdGroupCommand command = AddAdGroupCommand.builder()
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(500L)
            .build();

        // When
        AdGroup adGroup = useCase.execute(command);

        // Then
        assertThat(adGroup.getName()).isEqualTo("Product Category A");
        assertThat(adGroup.getBid()).isEqualTo(500L);
        assertThat(adGroup.getCampaignId()).isEqualTo("camp-1");
        assertThat(adGroup.getStatus()).isEqualTo(AdStatus.ACTIVE);
        assertThat(campaign.getAdGroups()).hasSize(1);

        verify(campaignRepository).findById("camp-1");
        verify(campaignRepository).save(campaign);
    }

    @Test
    void 존재하지_않는_캠페인에는_광고그룹을_추가할_수_없다() {
        // Given
        when(campaignRepository.findById("invalid-camp")).thenReturn(Optional.empty());

        AddAdGroupCommand command = AddAdGroupCommand.builder()
            .campaignId("invalid-camp")
            .name("Product Category A")
            .bid(500L)
            .build();

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(CampaignNotFoundException.class)
            .hasMessageContaining("캠페인을 찾을 수 없습니다");

        verify(campaignRepository, never()).save(any());
    }

    @Test
    void 광고그룹_ID가_자동_생성된다() {
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

        AddAdGroupCommand command = AddAdGroupCommand.builder()
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(500L)
            .build();

        // When
        AdGroup adGroup = useCase.execute(command);

        // Then
        assertThat(adGroup.getId()).isNotNull();
        assertThat(adGroup.getId()).startsWith("ag-");
    }
}
