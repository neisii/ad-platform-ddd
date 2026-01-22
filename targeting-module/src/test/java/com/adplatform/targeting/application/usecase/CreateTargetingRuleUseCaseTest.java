package com.adplatform.targeting.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.adplatform.targeting.application.dto.CreateTargetingRuleCommand;
import com.adplatform.targeting.domain.model.DeviceType;
import com.adplatform.targeting.domain.model.Gender;
import com.adplatform.targeting.domain.model.TargetingRule;
import com.adplatform.targeting.domain.repository.TargetingRuleRepository;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateTargetingRuleUseCaseTest {

    @Mock
    private TargetingRuleRepository targetingRuleRepository;

    @InjectMocks
    private CreateTargetingRuleUseCase createTargetingRuleUseCase;

    @BeforeEach
    void setUp() {
        lenient()
            .when(targetingRuleRepository.save(any(TargetingRule.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void 타겟팅_룰을_생성한다() {
        // Given
        CreateTargetingRuleCommand command =
            CreateTargetingRuleCommand.builder()
                .campaignId("camp-1")
                .ageMin(20)
                .ageMax(40)
                .gender(Gender.M)
                .geoTargets(Arrays.asList("KR", "Seoul"))
                .deviceTypes(
                    Arrays.asList(DeviceType.MOBILE, DeviceType.TABLET)
                )
                .keywords(Arrays.asList("tech", "gaming"))
                .build();

        // When
        TargetingRule result = createTargetingRuleUseCase.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCampaignId()).isEqualTo("camp-1");
        assertThat(result.getDemographics().getAgeMin()).isEqualTo(20);
        assertThat(result.getDemographics().getAgeMax()).isEqualTo(40);
        assertThat(result.getDemographics().getGender()).isEqualTo(Gender.M);
        assertThat(result.getGeoTargets()).containsExactly("KR", "Seoul");
        assertThat(result.getDeviceTypes()).containsExactly(
            DeviceType.MOBILE,
            DeviceType.TABLET
        );
        assertThat(result.getKeywords()).containsExactly("tech", "gaming");

        verify(targetingRuleRepository, times(1)).save(
            any(TargetingRule.class)
        );
    }

    @Test
    void 선택적_필드_없이_타겟팅_룰을_생성한다() {
        // Given
        CreateTargetingRuleCommand command =
            CreateTargetingRuleCommand.builder().campaignId("camp-1").build();

        // When
        TargetingRule result = createTargetingRuleUseCase.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCampaignId()).isEqualTo("camp-1");
        assertThat(result.getDemographics().getGender()).isEqualTo(Gender.ANY);
        assertThat(result.getGeoTargets()).isEmpty();
        assertThat(result.getDeviceTypes()).isEmpty();
        assertThat(result.getKeywords()).isEmpty();

        verify(targetingRuleRepository, times(1)).save(
            any(TargetingRule.class)
        );
    }

    @Test
    void 캠페인_ID가_없으면_생성할_수_없다() {
        // Given
        CreateTargetingRuleCommand command =
            CreateTargetingRuleCommand.builder().campaignId(null).build();

        // When & Then
        assertThatThrownBy(() -> createTargetingRuleUseCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("캠페인 ID는 필수입니다");

        verify(targetingRuleRepository, never()).save(any(TargetingRule.class));
    }
}
