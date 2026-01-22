package com.adplatform.campaign.adapter.rest;

import com.adplatform.campaign.adapter.rest.dto.*;
import com.adplatform.campaign.application.dto.AddAdGroupCommand;
import com.adplatform.campaign.application.dto.CreateCampaignCommand;
import com.adplatform.campaign.application.dto.UpdateCampaignStatusCommand;
import com.adplatform.campaign.application.usecase.AddAdGroupUseCase;
import com.adplatform.campaign.application.usecase.CreateCampaignUseCase;
import com.adplatform.campaign.application.usecase.UpdateCampaignStatusUseCase;
import com.adplatform.campaign.domain.model.AdGroup;
import com.adplatform.campaign.domain.model.Campaign;
import com.adplatform.campaign.domain.repository.CampaignRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Campaign REST Controller
 */
@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CreateCampaignUseCase createCampaignUseCase;
    private final UpdateCampaignStatusUseCase updateCampaignStatusUseCase;
    private final AddAdGroupUseCase addAdGroupUseCase;
    private final CampaignRepository campaignRepository;

    /**
     * 캠페인 생성
     */
    @PostMapping
    public ResponseEntity<CampaignResponse> createCampaign(
            @Valid @RequestBody CreateCampaignRequest request) {

        CreateCampaignCommand command = CreateCampaignCommand.builder()
            .advertiserId(request.getAdvertiserId())
            .name(request.getName())
            .dailyBudget(request.getDailyBudget())
            .totalBudget(request.getTotalBudget())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();

        Campaign campaign = createCampaignUseCase.execute(command);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CampaignResponse.from(campaign));
    }

    /**
     * 캠페인 조회
     */
    @GetMapping("/{campaignId}")
    public ResponseEntity<CampaignResponse> getCampaign(
            @PathVariable String campaignId) {

        return campaignRepository.findById(campaignId)
            .map(CampaignResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 광고주별 캠페인 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<CampaignResponse>> getCampaignsByAdvertiser(
            @RequestParam String advertiserId) {

        List<CampaignResponse> campaigns = campaignRepository
            .findByAdvertiserId(advertiserId)
            .stream()
            .map(CampaignResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(campaigns);
    }

    /**
     * 캠페인 상태 변경
     */
    @PatchMapping("/{campaignId}/status")
    public ResponseEntity<CampaignResponse> updateCampaignStatus(
            @PathVariable String campaignId,
            @Valid @RequestBody UpdateCampaignStatusRequest request) {

        UpdateCampaignStatusCommand command = UpdateCampaignStatusCommand.builder()
            .campaignId(campaignId)
            .status(request.getStatus())
            .build();

        Campaign campaign = updateCampaignStatusUseCase.execute(command);

        return ResponseEntity.ok(CampaignResponse.from(campaign));
    }

    /**
     * 광고그룹 추가
     */
    @PostMapping("/{campaignId}/ad-groups")
    public ResponseEntity<AdGroupResponse> addAdGroup(
            @PathVariable String campaignId,
            @Valid @RequestBody AddAdGroupRequest request) {

        AddAdGroupCommand command = AddAdGroupCommand.builder()
            .campaignId(campaignId)
            .name(request.getName())
            .bid(request.getBid())
            .build();

        AdGroup adGroup = addAdGroupUseCase.execute(command);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(AdGroupResponse.from(adGroup));
    }
}
