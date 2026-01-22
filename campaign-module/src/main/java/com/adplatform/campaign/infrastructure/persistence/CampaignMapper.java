package com.adplatform.campaign.infrastructure.persistence;

import com.adplatform.campaign.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Domain Model <-> JPA Entity 변환 Mapper
 */
@Component
public class CampaignMapper {

    /**
     * Domain Model -> JPA Entity
     */
    public CampaignEntity toEntity(Campaign campaign) {
        CampaignEntity entity = CampaignEntity.builder()
            .id(campaign.getId())
            .advertiserId(campaign.getAdvertiserId())
            .name(campaign.getName())
            .dailyBudget(campaign.getBudget().getDailyBudget())
            .totalBudget(campaign.getBudget().getTotalBudget())
            .spent(campaign.getBudget().getSpent())
            .status(campaign.getStatus())
            .startDate(campaign.getStartDate())
            .endDate(campaign.getEndDate())
            .build();

        // AdGroup 변환 및 추가
        campaign.getAdGroups().forEach(adGroup -> {
            AdGroupEntity adGroupEntity = toAdGroupEntity(adGroup);
            entity.addAdGroup(adGroupEntity);
        });

        return entity;
    }

    /**
     * JPA Entity -> Domain Model
     */
    public Campaign toDomain(CampaignEntity entity) {
        Budget budget = new Budget(
            entity.getDailyBudget(),
            entity.getTotalBudget(),
            entity.getSpent()
        );

        Campaign campaign = Campaign.builder()
            .id(entity.getId())
            .advertiserId(entity.getAdvertiserId())
            .name(entity.getName())
            .budget(budget)
            .status(entity.getStatus())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .build();

        // AdGroup 변환 및 추가
        entity.getAdGroups().forEach(adGroupEntity -> {
            AdGroup adGroup = toAdGroupDomain(adGroupEntity);
            campaign.addAdGroup(adGroup);
        });

        return campaign;
    }

    /**
     * AdGroup Domain -> Entity
     */
    private AdGroupEntity toAdGroupEntity(AdGroup adGroup) {
        AdGroupEntity entity = AdGroupEntity.builder()
            .id(adGroup.getId())
            .name(adGroup.getName())
            .bid(adGroup.getBid())
            .status(adGroup.getStatus())
            .build();

        // Ad 변환 및 추가
        adGroup.getAds().forEach(ad -> {
            AdEntity adEntity = toAdEntity(ad);
            entity.addAd(adEntity);
        });

        return entity;
    }

    /**
     * AdGroup Entity -> Domain
     */
    private AdGroup toAdGroupDomain(AdGroupEntity entity) {
        AdGroup adGroup = AdGroup.builder()
            .id(entity.getId())
            .campaignId(entity.getCampaign().getId())
            .name(entity.getName())
            .bid(entity.getBid())
            .status(entity.getStatus())
            .build();

        // Ad 변환 및 추가
        entity.getAds().forEach(adEntity -> {
            Ad ad = toAdDomain(adEntity);
            adGroup.addAd(ad);
        });

        return adGroup;
    }

    /**
     * Ad Domain -> Entity
     */
    private AdEntity toAdEntity(Ad ad) {
        return AdEntity.builder()
            .id(ad.getId())
            .title(ad.getTitle())
            .description(ad.getDescription())
            .landingUrl(ad.getLandingUrl())
            .status(ad.getStatus())
            .build();
    }

    /**
     * Ad Entity -> Domain
     */
    private Ad toAdDomain(AdEntity entity) {
        return Ad.builder()
            .id(entity.getId())
            .adGroupId(entity.getAdGroup().getId())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .landingUrl(entity.getLandingUrl())
            .status(entity.getStatus())
            .build();
    }

    /**
     * 기존 Entity를 Domain 변경사항으로 업데이트
     */
    public void updateEntity(CampaignEntity entity, Campaign campaign) {
        entity.updateFromDomain(
            campaign.getName(),
            campaign.getBudget().getDailyBudget(),
            campaign.getBudget().getTotalBudget(),
            campaign.getBudget().getSpent(),
            campaign.getStatus()
        );

        // AdGroup 동기화 (간단하게 clear & add)
        entity.getAdGroups().clear();
        campaign.getAdGroups().forEach(adGroup -> {
            AdGroupEntity adGroupEntity = toAdGroupEntity(adGroup);
            entity.addAdGroup(adGroupEntity);
        });
    }
}
