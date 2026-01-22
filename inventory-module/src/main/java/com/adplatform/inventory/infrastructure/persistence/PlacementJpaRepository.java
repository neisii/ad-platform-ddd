package com.adplatform.inventory.infrastructure.persistence;

import com.adplatform.inventory.domain.model.PlacementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 게재 위치 JPA Repository
 */
public interface PlacementJpaRepository extends JpaRepository<PlacementEntity, String> {

    /**
     * 퍼블리셔 ID로 게재 위치 목록 조회
     */
    List<PlacementEntity> findByPublisherId(String publisherId);

    /**
     * 상태로 게재 위치 목록 조회
     */
    List<PlacementEntity> findByStatus(PlacementStatus status);
}
