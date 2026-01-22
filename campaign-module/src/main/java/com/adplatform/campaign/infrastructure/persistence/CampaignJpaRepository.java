package com.adplatform.campaign.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Campaign JPA Repository
 * - Spring Data JPA 인터페이스
 */
public interface CampaignJpaRepository extends JpaRepository<CampaignEntity, String> {

    /**
     * 광고주 ID로 캠페인 목록 조회 (AdGroup까지 fetch join)
     */
    @Query("SELECT DISTINCT c FROM CampaignEntity c " +
           "LEFT JOIN FETCH c.adGroups ag " +
           "WHERE c.advertiserId = :advertiserId")
    List<CampaignEntity> findByAdvertiserIdWithAdGroups(@Param("advertiserId") String advertiserId);

    /**
     * 광고주 ID로 캠페인 목록 조회
     */
    List<CampaignEntity> findByAdvertiserId(String advertiserId);
}
