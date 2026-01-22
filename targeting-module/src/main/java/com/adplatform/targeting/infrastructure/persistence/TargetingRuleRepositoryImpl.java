package com.adplatform.targeting.infrastructure.persistence;

import com.adplatform.targeting.domain.model.TargetingRule;
import com.adplatform.targeting.domain.repository.TargetingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TargetingRule Repository 구현체
 * - Domain Repository 인터페이스 구현
 * - JPA Repository와 Mapper를 사용하여 영속성 처리
 */
@Repository
@RequiredArgsConstructor
public class TargetingRuleRepositoryImpl implements TargetingRuleRepository {

    private final TargetingRuleJpaRepository jpaRepository;
    private final TargetingRuleMapper mapper;

    @Override
    public TargetingRule save(TargetingRule targetingRule) {
        Optional<TargetingRuleEntity> existingEntity = jpaRepository.findById(targetingRule.getId());

        TargetingRuleEntity entity;
        if (existingEntity.isPresent()) {
            // 기존 엔티티 업데이트
            entity = existingEntity.get();
            mapper.updateEntity(entity, targetingRule);
        } else {
            // 새 엔티티 생성
            entity = mapper.toEntity(targetingRule);
        }

        TargetingRuleEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<TargetingRule> findById(String id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public List<TargetingRule> findByCampaignId(String campaignId) {
        return jpaRepository.findByCampaignId(campaignId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<TargetingRule> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
}
