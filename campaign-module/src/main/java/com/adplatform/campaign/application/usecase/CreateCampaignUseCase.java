package com.adplatform.campaign.application.usecase;

import com.adplatform.campaign.application.dto.CreateCampaignCommand;
import com.adplatform.campaign.domain.exception.AdvertiserNotFoundException;
import com.adplatform.campaign.domain.model.AdStatus;
import com.adplatform.campaign.domain.model.Budget;
import com.adplatform.campaign.domain.model.Campaign;
import com.adplatform.campaign.domain.repository.CampaignRepository;
import com.adplatform.campaign.infrastructure.client.AdvertiserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 캠페인 생성 Use Case
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreateCampaignUseCase {

    private final CampaignRepository campaignRepository;
    private final AdvertiserClient advertiserClient;

    public Campaign execute(CreateCampaignCommand command) {
        // 1. 광고주 존재 여부 확인
        if (!advertiserClient.exists(command.getAdvertiserId())) {
            throw AdvertiserNotFoundException.withId(command.getAdvertiserId());
        }

        // 2. Campaign Aggregate 생성
        Budget budget = new Budget(command.getDailyBudget(), command.getTotalBudget());

        Campaign campaign = Campaign.builder()
            .id(generateCampaignId())
            .advertiserId(command.getAdvertiserId())
            .name(command.getName())
            .budget(budget)
            .status(AdStatus.ACTIVE)
            .startDate(command.getStartDate())
            .endDate(command.getEndDate())
            .build();

        // 3. 저장
        return campaignRepository.save(campaign);
    }

    private String generateCampaignId() {
        return "camp-" + UUID.randomUUID().toString();
    }
}
