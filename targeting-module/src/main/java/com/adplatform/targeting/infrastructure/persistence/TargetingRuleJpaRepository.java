package com.adplatform.targeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * TargetingRule JPA Repository
 */
public interface TargetingRuleJpaRepository extends JpaRepository<TargetingRuleEntity, String> {

    List<TargetingRuleEntity> findByCampaignId(String campaignId);
}
