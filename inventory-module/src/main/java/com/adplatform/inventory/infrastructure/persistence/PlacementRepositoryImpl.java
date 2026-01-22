package com.adplatform.inventory.infrastructure.persistence;

import com.adplatform.inventory.domain.model.Placement;
import com.adplatform.inventory.domain.model.PlacementStatus;
import com.adplatform.inventory.domain.repository.PlacementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 게재 위치 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class PlacementRepositoryImpl implements PlacementRepository {

    private final PlacementJpaRepository jpaRepository;

    @Override
    public Placement save(Placement placement) {
        PlacementEntity entity = PlacementEntity.from(placement);
        PlacementEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Placement> findById(String id) {
        return jpaRepository.findById(id)
            .map(PlacementEntity::toDomain);
    }

    @Override
    public List<Placement> findByPublisherId(String publisherId) {
        return jpaRepository.findByPublisherId(publisherId)
            .stream()
            .map(PlacementEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Placement> findAllActive() {
        return jpaRepository.findByStatus(PlacementStatus.ACTIVE)
            .stream()
            .map(PlacementEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }
}
