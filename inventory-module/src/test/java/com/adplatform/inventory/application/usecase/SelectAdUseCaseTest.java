package com.adplatform.inventory.application.usecase;

import com.adplatform.inventory.application.dto.AdSelectionResult;
import com.adplatform.inventory.application.dto.SelectAdCommand;
import com.adplatform.inventory.domain.exception.InactivePlacementException;
import com.adplatform.inventory.domain.exception.NoAdsAvailableException;
import com.adplatform.inventory.domain.exception.PlacementNotFoundException;
import com.adplatform.inventory.domain.model.Placement;
import com.adplatform.inventory.domain.model.PlacementStatus;
import com.adplatform.inventory.domain.model.PlacementType;
import com.adplatform.inventory.domain.model.PricingModel;
import com.adplatform.inventory.domain.repository.PlacementRepository;
import com.adplatform.inventory.infrastructure.client.CampaignClient;
import com.adplatform.inventory.infrastructure.client.CampaignDto;
import com.adplatform.inventory.infrastructure.client.TargetingClient;
import com.adplatform.inventory.infrastructure.client.TargetingMatchDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SelectAdUseCase 테스트")
class SelectAdUseCaseTest {

    @Mock
    private PlacementRepository placementRepository;

    @Mock
    private CampaignClient campaignClient;

    @Mock
    private TargetingClient targetingClient;

    @InjectMocks
    private SelectAdUseCase useCase;

    @Test
    @DisplayName("광고를 성공적으로 선택할 수 있다")
    void execute_Success() {
        // given
        Placement placement = createActivePlacement();
        SelectAdCommand command = createSelectAdCommand();

        List<CampaignDto> campaigns = Arrays.asList(
            createCampaignDto("campaign-1", 5000L),
            createCampaignDto("campaign-2", 3000L)
        );

        when(placementRepository.findById("placement-1"))
            .thenReturn(Optional.of(placement));
        when(campaignClient.getActiveCampaigns())
            .thenReturn(campaigns);
        when(targetingClient.matchTargeting(eq("campaign-1"), any()))
            .thenReturn(createTargetingMatch("campaign-1", 80, true));
        when(targetingClient.matchTargeting(eq("campaign-2"), any()))
            .thenReturn(createTargetingMatch("campaign-2", 90, true));

        // when
        AdSelectionResult result = useCase.execute(command);

        // then
        assertThat(result).isNotNull();
        // campaign-2: 3000 * 90 / 100 = 2700
        // campaign-1: 5000 * 80 / 100 = 4000 <- 선택됨
        assertThat(result.getCampaignId()).isEqualTo("campaign-1");
        assertThat(result.getBid()).isEqualTo(5000L);
        assertThat(result.getMatchScore()).isEqualTo(80);
        assertThat(result.getImpressionToken()).isNotBlank();
    }

    @Test
    @DisplayName("매칭 스코어가 높은 광고를 선택한다")
    void execute_SelectsHighestScoringAd() {
        // given
        Placement placement = createActivePlacement();
        SelectAdCommand command = createSelectAdCommand();

        List<CampaignDto> campaigns = Arrays.asList(
            createCampaignDto("campaign-1", 2000L),
            createCampaignDto("campaign-2", 3000L),
            createCampaignDto("campaign-3", 1000L)
        );

        when(placementRepository.findById("placement-1"))
            .thenReturn(Optional.of(placement));
        when(campaignClient.getActiveCampaigns())
            .thenReturn(campaigns);
        when(targetingClient.matchTargeting(eq("campaign-1"), any()))
            .thenReturn(createTargetingMatch("campaign-1", 50, true));
        when(targetingClient.matchTargeting(eq("campaign-2"), any()))
            .thenReturn(createTargetingMatch("campaign-2", 100, true));
        when(targetingClient.matchTargeting(eq("campaign-3"), any()))
            .thenReturn(createTargetingMatch("campaign-3", 30, true));

        // when
        AdSelectionResult result = useCase.execute(command);

        // then
        // campaign-1: 2000 * 50 / 100 = 1000
        // campaign-2: 3000 * 100 / 100 = 3000 <- 선택됨
        // campaign-3: 1000 * 30 / 100 = 300
        assertThat(result.getCampaignId()).isEqualTo("campaign-2");
        assertThat(result.getMatchScore()).isEqualTo(100);
    }

    @Test
    @DisplayName("매칭되지 않은 광고는 제외된다")
    void execute_ExcludesNonMatchedAds() {
        // given
        Placement placement = createActivePlacement();
        SelectAdCommand command = createSelectAdCommand();

        List<CampaignDto> campaigns = Arrays.asList(
            createCampaignDto("campaign-1", 5000L),
            createCampaignDto("campaign-2", 3000L)
        );

        when(placementRepository.findById("placement-1"))
            .thenReturn(Optional.of(placement));
        when(campaignClient.getActiveCampaigns())
            .thenReturn(campaigns);
        when(targetingClient.matchTargeting(eq("campaign-1"), any()))
            .thenReturn(createTargetingMatch("campaign-1", 0, false));
        when(targetingClient.matchTargeting(eq("campaign-2"), any()))
            .thenReturn(createTargetingMatch("campaign-2", 80, true));

        // when
        AdSelectionResult result = useCase.execute(command);

        // then
        assertThat(result.getCampaignId()).isEqualTo("campaign-2");
    }

    @Test
    @DisplayName("게재 위치가 존재하지 않으면 예외가 발생한다")
    void execute_PlacementNotFound_ThrowsException() {
        // given
        SelectAdCommand command = createSelectAdCommand();

        when(placementRepository.findById("placement-1"))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(PlacementNotFoundException.class);
    }

    @Test
    @DisplayName("게재 위치가 비활성 상태면 예외가 발생한다")
    void execute_InactivePlacement_ThrowsException() {
        // given
        Placement placement = Placement.builder()
            .id("placement-1")
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .status(PlacementStatus.PAUSED)
            .build();

        SelectAdCommand command = createSelectAdCommand();

        when(placementRepository.findById("placement-1"))
            .thenReturn(Optional.of(placement));

        // when & then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(InactivePlacementException.class);
    }

    @Test
    @DisplayName("선택 가능한 광고가 없으면 예외가 발생한다")
    void execute_NoAdsAvailable_ThrowsException() {
        // given
        Placement placement = createActivePlacement();
        SelectAdCommand command = createSelectAdCommand();

        when(placementRepository.findById("placement-1"))
            .thenReturn(Optional.of(placement));
        when(campaignClient.getActiveCampaigns())
            .thenReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(NoAdsAvailableException.class);
    }

    @Test
    @DisplayName("모든 광고가 매칭 실패하면 예외가 발생한다")
    void execute_AllAdsNotMatched_ThrowsException() {
        // given
        Placement placement = createActivePlacement();
        SelectAdCommand command = createSelectAdCommand();

        List<CampaignDto> campaigns = Arrays.asList(
            createCampaignDto("campaign-1", 5000L),
            createCampaignDto("campaign-2", 3000L)
        );

        when(placementRepository.findById("placement-1"))
            .thenReturn(Optional.of(placement));
        when(campaignClient.getActiveCampaigns())
            .thenReturn(campaigns);
        when(targetingClient.matchTargeting(anyString(), any()))
            .thenReturn(createTargetingMatch("campaign-1", 0, false));

        // when & then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(NoAdsAvailableException.class);
    }

    // Helper methods
    private Placement createActivePlacement() {
        return Placement.builder()
            .id("placement-1")
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .status(PlacementStatus.ACTIVE)
            .build();
    }

    private SelectAdCommand createSelectAdCommand() {
        return SelectAdCommand.builder()
            .placementId("placement-1")
            .userContext(SelectAdCommand.UserContext.builder()
                .userId("user-1")
                .age(25)
                .gender("M")
                .country("KR")
                .city("Seoul")
                .deviceType("MOBILE")
                .keywords(Arrays.asList("sports", "technology"))
                .build())
            .build();
    }

    private CampaignDto createCampaignDto(String campaignId, Long bidAmount) {
        return CampaignDto.builder()
            .id(campaignId)
            .advertiserId("adv-1")
            .name("Campaign " + campaignId)
            .bidAmount(bidAmount)
            .status("ACTIVE")
            .build();
    }

    private TargetingMatchDto createTargetingMatch(String campaignId, int score, boolean matched) {
        return TargetingMatchDto.builder()
            .campaignId(campaignId)
            .matchScore(score)
            .matched(matched)
            .build();
    }
}
