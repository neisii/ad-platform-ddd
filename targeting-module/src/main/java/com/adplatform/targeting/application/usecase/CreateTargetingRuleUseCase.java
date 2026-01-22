package com.adplatform.targeting.application.usecase;

import com.adplatform.targeting.application.dto.CreateTargetingRuleCommand;
import com.adplatform.targeting.domain.model.Demographics;
import com.adplatform.targeting.domain.model.TargetingRule;
import com.adplatform.targeting.domain.repository.TargetingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 타겟팅 룰 생성 Use Case
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreateTargetingRuleUseCase {

    private final TargetingRuleRepository targetingRuleRepository;

    public TargetingRule execute(CreateTargetingRuleCommand command) {
        // 1. Demographics Value Object 생성
        Demographics demographics = Demographics.of(
            command.getAgeMin(),
            command.getAgeMax(),
            command.getGender()
        );

        // 2. TargetingRule Aggregate 생성
        TargetingRule targetingRule = TargetingRule.builder()
            .id(generateTargetingRuleId())
            .campaignId(command.getCampaignId())
            .demographics(demographics)
            .geoTargets(command.getGeoTargets())
            .deviceTypes(command.getDeviceTypes())
            .keywords(command.getKeywords())
            .build();

        // 3. 저장
        return targetingRuleRepository.save(targetingRule);
    }

    private String generateTargetingRuleId() {
        return "rule-" + UUID.randomUUID().toString();
    }
}
