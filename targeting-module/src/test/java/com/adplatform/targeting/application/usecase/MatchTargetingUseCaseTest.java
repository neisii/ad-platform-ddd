package com.adplatform.targeting.application.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.adplatform.targeting.application.dto.MatchTargetingCommand;
import com.adplatform.targeting.application.dto.TargetingMatchResult;
import com.adplatform.targeting.domain.model.Demographics;
import com.adplatform.targeting.domain.model.DeviceType;
import com.adplatform.targeting.domain.model.Gender;
import com.adplatform.targeting.domain.model.TargetingRule;
import com.adplatform.targeting.domain.repository.TargetingRuleRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchTargetingUseCaseTest {

    @Mock
    private TargetingRuleRepository targetingRuleRepository;

    @InjectMocks
    private MatchTargetingUseCase matchTargetingUseCase;

    @Test
    void 사용자_컨텍스트와_일치하는_타겟팅_룰을_찾는다() {
        // Given
        TargetingRule rule1 = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("KR"))
            .deviceTypes(Arrays.asList(DeviceType.MOBILE))
            .keywords(Arrays.asList("tech"))
            .build();

        TargetingRule rule2 = TargetingRule.builder()
            .id("rule-2")
            .campaignId("camp-2")
            .demographics(Demographics.of(30, 50, Gender.F))
            .geoTargets(Arrays.asList("US"))
            .build();

        when(targetingRuleRepository.findAll()).thenReturn(
            Arrays.asList(rule1, rule2)
        );

        MatchTargetingCommand command = MatchTargetingCommand.builder()
            .age(30)
            .gender(Gender.M)
            .country("KR")
            .deviceType(DeviceType.MOBILE)
            .keywords(Arrays.asList("tech", "gaming"))
            .build();

        // When
        List<TargetingMatchResult> results = matchTargetingUseCase.execute(
            command
        );

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(r -> r.getCampaignId().equals("camp-1"));
        assertThat(results.get(0).getMatchScore()).isGreaterThan(0);

        verify(targetingRuleRepository, times(1)).findAll();
    }

    @Test
    void 매칭_스코어가_높은_순으로_정렬된다() {
        // Given
        // rule1: demographics만 일치 (부분 매칭)
        TargetingRule rule1 = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("US")) // 불일치
            .build();

        // rule2: 모든 조건 일치 (완전 매칭)
        TargetingRule rule2 = TargetingRule.builder()
            .id("rule-2")
            .campaignId("camp-2")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("KR"))
            .deviceTypes(Arrays.asList(DeviceType.MOBILE))
            .keywords(Arrays.asList("tech"))
            .build();

        when(targetingRuleRepository.findAll()).thenReturn(
            Arrays.asList(rule1, rule2)
        );

        MatchTargetingCommand command = MatchTargetingCommand.builder()
            .age(30)
            .gender(Gender.M)
            .country("KR")
            .deviceType(DeviceType.MOBILE)
            .keywords(Arrays.asList("tech"))
            .build();

        // When
        List<TargetingMatchResult> results = matchTargetingUseCase.execute(
            command
        );

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getCampaignId()).isEqualTo("camp-2"); // 더 높은 스코어
        assertThat(results.get(1).getCampaignId()).isEqualTo("camp-1");
        assertThat(results.get(0).getMatchScore()).isGreaterThan(
            results.get(1).getMatchScore()
        );

        verify(targetingRuleRepository, times(1)).findAll();
    }

    @Test
    void 매칭_스코어가_0인_룰은_제외된다() {
        // Given
        TargetingRule rule1 = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("KR"))
            .build();

        TargetingRule rule2 = TargetingRule.builder()
            .id("rule-2")
            .campaignId("camp-2")
            .demographics(Demographics.of(50, 60, Gender.F)) // 조건 불일치
            .geoTargets(Arrays.asList("US")) // 조건 불일치
            .build();

        when(targetingRuleRepository.findAll()).thenReturn(
            Arrays.asList(rule1, rule2)
        );

        MatchTargetingCommand command = MatchTargetingCommand.builder()
            .age(30)
            .gender(Gender.M)
            .country("KR")
            .build();

        // When
        List<TargetingMatchResult> results = matchTargetingUseCase.execute(
            command
        );

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCampaignId()).isEqualTo("camp-1");

        verify(targetingRuleRepository, times(1)).findAll();
    }

    @Test
    void 타겟팅_룰이_없으면_빈_리스트를_반환한다() {
        // Given
        when(targetingRuleRepository.findAll()).thenReturn(Arrays.asList());

        MatchTargetingCommand command = MatchTargetingCommand.builder()
            .age(30)
            .gender(Gender.M)
            .build();

        // When
        List<TargetingMatchResult> results = matchTargetingUseCase.execute(
            command
        );

        // Then
        assertThat(results).isEmpty();

        verify(targetingRuleRepository, times(1)).findAll();
    }
}
