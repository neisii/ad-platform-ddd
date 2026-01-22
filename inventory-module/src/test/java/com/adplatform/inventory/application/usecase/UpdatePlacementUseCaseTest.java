package com.adplatform.inventory.application.usecase;

import com.adplatform.inventory.application.dto.UpdatePlacementCommand;
import com.adplatform.inventory.domain.exception.PlacementNotFoundException;
import com.adplatform.inventory.domain.model.Placement;
import com.adplatform.inventory.domain.model.PlacementType;
import com.adplatform.inventory.domain.model.PricingModel;
import com.adplatform.inventory.domain.repository.PlacementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePlacementUseCase 테스트")
class UpdatePlacementUseCaseTest {

    @Mock
    private PlacementRepository placementRepository;

    @InjectMocks
    private UpdatePlacementUseCase useCase;

    @Test
    @DisplayName("게재 위치 정보를 업데이트할 수 있다")
    void execute_ValidCommand_Success() {
        // given
        Placement existingPlacement = Placement.builder()
            .id("placement-1")
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .build();

        UpdatePlacementCommand command = UpdatePlacementCommand.builder()
            .placementId("placement-1")
            .name("업데이트된 배너")
            .placementType(PlacementType.VIDEO)
            .pricingModel(PricingModel.CPC)
            .basePrice(2000L)
            .build();

        when(placementRepository.findById("placement-1"))
            .thenReturn(Optional.of(existingPlacement));
        when(placementRepository.save(any(Placement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Placement result = useCase.execute(command);

        // then
        assertThat(result.getName()).isEqualTo("업데이트된 배너");
        assertThat(result.getPlacementType()).isEqualTo(PlacementType.VIDEO);
        assertThat(result.getPricingModel()).isEqualTo(PricingModel.CPC);
        assertThat(result.getBasePrice()).isEqualTo(2000L);

        verify(placementRepository, times(1)).findById("placement-1");
        verify(placementRepository, times(1)).save(any(Placement.class));
    }

    @Test
    @DisplayName("존재하지 않는 게재 위치 업데이트 시 예외가 발생한다")
    void execute_PlacementNotFound_ThrowsException() {
        // given
        UpdatePlacementCommand command = UpdatePlacementCommand.builder()
            .placementId("non-existent")
            .name("업데이트된 배너")
            .placementType(PlacementType.VIDEO)
            .pricingModel(PricingModel.CPC)
            .basePrice(2000L)
            .build();

        when(placementRepository.findById("non-existent"))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(PlacementNotFoundException.class);

        verify(placementRepository, times(1)).findById("non-existent");
        verify(placementRepository, never()).save(any(Placement.class));
    }
}
