package com.adplatform.campaign.domain.repository;

import com.adplatform.campaign.domain.model.Campaign;

import java.util.List;
import java.util.Optional;

/**
 * Campaign Aggregate Repository
 * - Domain layer의 인터페이스
 * - Infrastructure layer에서 구현
 */
public interface CampaignRepository {

    /**
     * 캠페인 저장
     */
    Campaign save(Campaign campaign);

    /**
     * ID로 캠페인 조회
     */
    Optional<Campaign> findById(String id);

    /**
     * 광고주 ID로 캠페인 목록 조회
     */
    List<Campaign> findByAdvertiserId(String advertiserId);

    /**
     * 캠페인 존재 여부 확인
     */
    boolean existsById(String id);

    /**
     * 캠페인 삭제
     */
    void deleteById(String id);
}
