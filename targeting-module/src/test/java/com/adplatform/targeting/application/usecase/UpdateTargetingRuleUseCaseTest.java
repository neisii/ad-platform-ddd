package com.adplatform.targeting.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.adplatform.targeting.application.dto.UpdateTargetingRuleCommand;
import com.adplatform.targeting.domain.exception.TargetingRuleNotFoundException;
import com.adplatform.targeting.domain.model.Demographics;
import com.adplatform.targeting.domain.model.DeviceType;
import com.adplatform.targeting.domain.model.Gender;
import com.adplatform.targeting.domain.model.TargetingRule;
import com.adplatform.targeting.domain.repository.TargetingRuleRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTargetingRuleUseCaseTest {

    @Mock
    private TargetingRuleRepository targetingRuleRepository;

    @InjectMocks
    private UpdateTargetingRuleUseCase updateTargetingRuleUseCase;

    @BeforeEach
    void setUp() {
        lenient()
            .when(targetingRuleRepository.save(any(TargetingRule.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void 타겟팅_룰을_업데이트한다() {
        // Given
        TargetingRule existingRule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("KR"))
            .deviceTypes(Arrays.asList(DeviceType.MOBILE))
            .keywords(Arrays.asList("tech"))
            .build();

        when(targetingRuleRepository.findById("rule-1")).thenReturn(
            Optional.of(existingRule)
        );

        UpdateTargetingRuleCommand command =
            UpdateTargetingRuleCommand.builder()
                .targetingRuleId("rule-1")
                .ageMin(30)
                .ageMax(50)
                .gender(Gender.F)
                .geoTargets(Arrays.asList("US", "NewYork"))
                .deviceTypes(
                    Arrays.asList(DeviceType.DESKTOP, DeviceType.TABLET)
                )
                .keywords(Arrays.asList("sports", "fitness"))
                .build();

        // When
        TargetingRule result = updateTargetingRuleUseCase.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("rule-1");
        assertThat(result.getDemographics().getAgeMin()).isEqualTo(30);
        assertThat(result.getDemographics().getAgeMax()).isEqualTo(50);
        assertThat(result.getDemographics().getGender()).isEqualTo(Gender.F);
        assertThat(result.getGeoTargets()).containsExactly("US", "NewYork");
        assertThat(result.getDeviceTypes()).containsExactly(
            DeviceType.DESKTOP,
            DeviceType.TABLET
        );
        assertThat(result.getKeywords()).containsExactly("sports", "fitness");

        verify(targetingRuleRepository, times(1)).findById("rule-1");
        verify(targetingRuleRepository, times(1)).save(
            any(TargetingRule.class)
        );
    }

    @Test
    void 존재하지_않는_타겟팅_룰을_업데이트하면_예외가_발생한다() {
        // Given
        when(targetingRuleRepository.findById("non-existent")).thenReturn(
            Optional.empty()
        );

        UpdateTargetingRuleCommand command =
            UpdateTargetingRuleCommand.builder()
                .targetingRuleId("non-existent")
                .ageMin(30)
                .ageMax(50)
                .gender(Gender.F)
                .build();

        // When & Then
        assertThatThrownBy(() -> updateTargetingRuleUseCase.execute(command))
            .isInstanceOf(TargetingRuleNotFoundException.class)
            .hasMessageContaining("타겟팅 룰을 찾을 수 없습니다");

        verify(targetingRuleRepository, times(1)).findById("non-existent");
        verify(targetingRuleRepository, never()).save(any(TargetingRule.class));
    }
}
