package com.adplatform.targeting.domain.repository;

import com.adplatform.targeting.domain.model.TargetingRule;

import java.util.List;
import java.util.Optional;

/**
 * TargetingRule Repository Interface
 */
public interface TargetingRuleRepository {

    TargetingRule save(TargetingRule targetingRule);

    Optional<TargetingRule> findById(String id);

    List<TargetingRule> findByCampaignId(String campaignId);

    List<TargetingRule> findAll();

    boolean existsById(String id);

    void deleteById(String id);
}
