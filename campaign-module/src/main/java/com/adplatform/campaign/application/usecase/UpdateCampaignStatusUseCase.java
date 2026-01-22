package com.adplatform.campaign.application.usecase;

import com.adplatform.campaign.application.dto.UpdateCampaignStatusCommand;
import com.adplatform.campaign.domain.exception.CampaignNotFoundException;
import com.adplatform.campaign.domain.model.Campaign;
import com.adplatform.campaign.domain.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 캠페인 상태 변경 Use Case
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCampaignStatusUseCase {

    private final CampaignRepository campaignRepository;

    public Campaign execute(UpdateCampaignStatusCommand command) {
        // 1. 캠페인 조회
        Campaign campaign = campaignRepository.findById(command.getCampaignId())
            .orElseThrow(() -> CampaignNotFoundException.withId(command.getCampaignId()));

        // 2. 상태 변경
        campaign.updateStatus(command.getStatus());

        // 3. 저장
        return campaignRepository.save(campaign);
    }
}
