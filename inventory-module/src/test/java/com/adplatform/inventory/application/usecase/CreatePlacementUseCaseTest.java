package com.adplatform.inventory.application.usecase;

import com.adplatform.inventory.application.dto.CreatePlacementCommand;
import com.adplatform.inventory.domain.model.Placement;
import com.adplatform.inventory.domain.model.PlacementStatus;
import com.adplatform.inventory.domain.model.PlacementType;
import com.adplatform.inventory.domain.model.PricingModel;
import com.adplatform.inventory.domain.repository.PlacementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePlacementUseCase 테스트")
class CreatePlacementUseCaseTest {

    @Mock
    private PlacementRepository placementRepository;

    @InjectMocks
    private CreatePlacementUseCase useCase;

    @BeforeEach
    void setUp() {
        // Mock 설정: save 호출 시 동일한 객체 반환
        when(placementRepository.save(any(Placement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("유효한 커맨드로 게재 위치를 생성할 수 있다")
    void execute_ValidCommand_Success() {
        // given
        CreatePlacementCommand command = CreatePlacementCommand.builder()
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .build();

        // when
        Placement result = useCase.execute(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("메인 배너");
        assertThat(result.getPublisherId()).isEqualTo("pub-1");
        assertThat(result.getPlacementType()).isEqualTo(PlacementType.BANNER);
        assertThat(result.getPricingModel()).isEqualTo(PricingModel.CPM);
        assertThat(result.getBasePrice()).isEqualTo(1000L);
        assertThat(result.getStatus()).isEqualTo(PlacementStatus.ACTIVE);

        verify(placementRepository, times(1)).save(any(Placement.class));
    }

    @Test
    @DisplayName("다양한 게재 위치 타입으로 생성할 수 있다")
    void execute_DifferentPlacementTypes_Success() {
        // given - VIDEO 타입
        CreatePlacementCommand videoCommand = CreatePlacementCommand.builder()
            .name("비디오 광고")
            .publisherId("pub-1")
            .placementType(PlacementType.VIDEO)
            .pricingModel(PricingModel.CPC)
            .basePrice(500L)
            .build();

        // when
        Placement videoResult = useCase.execute(videoCommand);

        // then
        assertThat(videoResult.getPlacementType()).isEqualTo(PlacementType.VIDEO);
        assertThat(videoResult.getPricingModel()).isEqualTo(PricingModel.CPC);

        // given - NATIVE 타입
        CreatePlacementCommand nativeCommand = CreatePlacementCommand.builder()
            .name("네이티브 광고")
            .publisherId("pub-1")
            .placementType(PlacementType.NATIVE)
            .pricingModel(PricingModel.CPA)
            .basePrice(2000L)
            .build();

        // when
        Placement nativeResult = useCase.execute(nativeCommand);

        // then
        assertThat(nativeResult.getPlacementType()).isEqualTo(PlacementType.NATIVE);
        assertThat(nativeResult.getPricingModel()).isEqualTo(PricingModel.CPA);
    }
}
