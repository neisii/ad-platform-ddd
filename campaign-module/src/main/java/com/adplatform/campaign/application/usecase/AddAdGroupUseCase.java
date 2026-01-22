package com.adplatform.campaign.application.usecase;

import com.adplatform.campaign.application.dto.AddAdGroupCommand;
import com.adplatform.campaign.domain.exception.CampaignNotFoundException;
import com.adplatform.campaign.domain.model.AdGroup;
import com.adplatform.campaign.domain.model.AdStatus;
import com.adplatform.campaign.domain.model.Campaign;
import com.adplatform.campaign.domain.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 광고그룹 추가 Use Case
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AddAdGroupUseCase {

    private final CampaignRepository campaignRepository;

    public AdGroup execute(AddAdGroupCommand command) {
        // 1. 캠페인 조회
        Campaign campaign = campaignRepository.findById(command.getCampaignId())
            .orElseThrow(() -> CampaignNotFoundException.withId(command.getCampaignId()));

        // 2. AdGroup 생성
        AdGroup adGroup = AdGroup.builder()
            .id(generateAdGroupId())
            .campaignId(campaign.getId())
            .name(command.getName())
            .bid(command.getBid())
            .status(AdStatus.ACTIVE)
            .build();

        // 3. Campaign에 추가
        campaign.addAdGroup(adGroup);

        // 4. 저장
        campaignRepository.save(campaign);

        return adGroup;
    }

    private String generateAdGroupId() {
        return "ag-" + UUID.randomUUID().toString();
    }
}
