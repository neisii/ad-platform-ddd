package com.adplatform.targeting.application.usecase;

import com.adplatform.targeting.application.dto.MatchTargetingCommand;
import com.adplatform.targeting.application.dto.TargetingMatchResult;
import com.adplatform.targeting.domain.model.TargetingRule;
import com.adplatform.targeting.domain.model.UserContext;
import com.adplatform.targeting.domain.repository.TargetingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 타겟팅 매칭 Use Case
 * - 사용자 컨텍스트에 맞는 타겟팅 룰을 찾아서 매칭 스코어와 함께 반환
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchTargetingUseCase {

    private final TargetingRuleRepository targetingRuleRepository;

    public List<TargetingMatchResult> execute(MatchTargetingCommand command) {
        // 1. UserContext Value Object 생성
        UserContext userContext = UserContext.builder()
            .age(command.getAge())
            .gender(command.getGender())
            .country(command.getCountry())
            .city(command.getCity())
            .deviceType(command.getDeviceType())
            .keywords(command.getKeywords())
            .build();

        // 2. 모든 타겟팅 룰 조회
        List<TargetingRule> allRules = targetingRuleRepository.findAll();

        // 3. 각 룰에 대해 매칭 스코어 계산
        return allRules.stream()
            .map(rule -> {
                int score = rule.matchScore(userContext);
                return TargetingMatchResult.of(rule.getId(), rule.getCampaignId(), score);
            })
            .filter(result -> result.getMatchScore() > 0) // 스코어가 0보다 큰 것만
            .sorted((r1, r2) -> Integer.compare(r2.getMatchScore(), r1.getMatchScore())) // 스코어 내림차순 정렬
            .collect(Collectors.toList());
    }
}
