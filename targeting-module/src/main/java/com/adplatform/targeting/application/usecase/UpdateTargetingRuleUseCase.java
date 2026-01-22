package com.adplatform.targeting.application.usecase;

import com.adplatform.targeting.application.dto.UpdateTargetingRuleCommand;
import com.adplatform.targeting.domain.exception.TargetingRuleNotFoundException;
import com.adplatform.targeting.domain.model.Demographics;
import com.adplatform.targeting.domain.model.TargetingRule;
import com.adplatform.targeting.domain.repository.TargetingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 타겟팅 룰 업데이트 Use Case
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateTargetingRuleUseCase {

    private final TargetingRuleRepository targetingRuleRepository;

    public TargetingRule execute(UpdateTargetingRuleCommand command) {
        // 1. 타겟팅 룰 조회
        TargetingRule targetingRule = targetingRuleRepository
            .findById(command.getTargetingRuleId())
            .orElseThrow(() -> TargetingRuleNotFoundException.withId(command.getTargetingRuleId()));

        // 2. Demographics Value Object 생성
        Demographics demographics = Demographics.of(
            command.getAgeMin(),
            command.getAgeMax(),
            command.getGender()
        );

        // 3. 타겟팅 룰 업데이트
        targetingRule.update(
            demographics,
            command.getGeoTargets(),
            command.getDeviceTypes(),
            command.getKeywords()
        );

        // 4. 저장
        return targetingRuleRepository.save(targetingRule);
    }
}
