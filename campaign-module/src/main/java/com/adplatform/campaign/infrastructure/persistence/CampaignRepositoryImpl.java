package com.adplatform.campaign.infrastructure.persistence;

import com.adplatform.campaign.domain.model.Campaign;
import com.adplatform.campaign.domain.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Campaign Repository 구현체
 * - Domain Repository 인터페이스 구현
 * - JPA Repository와 Mapper를 사용하여 영속성 처리
 */
@Repository
@RequiredArgsConstructor
public class CampaignRepositoryImpl implements CampaignRepository {

    private final CampaignJpaRepository jpaRepository;
    private final CampaignMapper mapper;

    @Override
    public Campaign save(Campaign campaign) {
        Optional<CampaignEntity> existingEntity = jpaRepository.findById(campaign.getId());

        CampaignEntity entity;
        if (existingEntity.isPresent()) {
            // 기존 엔티티 업데이트
            entity = existingEntity.get();
            mapper.updateEntity(entity, campaign);
        } else {
            // 새 엔티티 생성
            entity = mapper.toEntity(campaign);
        }

        CampaignEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Campaign> findById(String id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public List<Campaign> findByAdvertiserId(String advertiserId) {
        return jpaRepository.findByAdvertiserIdWithAdGroups(advertiserId).stream()
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
