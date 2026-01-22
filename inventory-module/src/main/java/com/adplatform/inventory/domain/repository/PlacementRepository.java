package com.adplatform.inventory.domain.repository;

import com.adplatform.inventory.domain.model.Placement;
import java.util.List;
import java.util.Optional;

/**
 * 게재 위치 저장소 인터페이스 (Port)
 */
public interface PlacementRepository {
    /**
     * 게재 위치 저장
     */
    Placement save(Placement placement);

    /**
     * ID로 게재 위치 조회
     */
    Optional<Placement> findById(String id);

    /**
     * 퍼블리셔 ID로 게재 위치 목록 조회
     */
    List<Placement> findByPublisherId(String publisherId);

    /**
     * 모든 활성 게재 위치 조회
     */
    List<Placement> findAllActive();

    /**
     * ID 존재 여부 확인
     */
    boolean existsById(String id);
}
