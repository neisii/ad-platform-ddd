package com.adplatform.inventory.application.usecase;

import com.adplatform.inventory.application.dto.AdSelectionResult;
import com.adplatform.inventory.application.dto.SelectAdCommand;
import com.adplatform.inventory.domain.exception.InactivePlacementException;
import com.adplatform.inventory.domain.exception.NoAdsAvailableException;
import com.adplatform.inventory.domain.exception.PlacementNotFoundException;
import com.adplatform.inventory.domain.model.AdSelection;
import com.adplatform.inventory.domain.model.Placement;
import com.adplatform.inventory.domain.model.SelectedAd;
import com.adplatform.inventory.domain.repository.PlacementRepository;
import com.adplatform.inventory.infrastructure.client.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 광고 선택 유스케이스 (핵심 광고 선택 로직)
 *
 * 프로세스:
 * 1. 게재 위치 검증 (존재 여부, 활성 상태)
 * 2. Campaign Service에서 활성 캠페인 목록 조회
 * 3. Targeting Service로 각 캠페인의 타겟팅 매칭 수행
 * 4. 랭킹 스코어 계산 (bid * matchScore / 100)
 * 5. 최고 점수의 광고 선택 및 반환
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SelectAdUseCase {

    private final PlacementRepository placementRepository;
    private final CampaignClient campaignClient;
    private final TargetingClient targetingClient;

    @Transactional(readOnly = true)
    public AdSelectionResult execute(SelectAdCommand command) {
        log.info("Selecting ad for placement: {}", command.getPlacementId());

        // 1. 게재 위치 검증
        Placement placement = validatePlacement(command.getPlacementId());

        // 2. 활성 캠페인 목록 조회
        List<CampaignDto> activeCampaigns = campaignClient.getActiveCampaigns();
        if (activeCampaigns.isEmpty()) {
            log.warn("No active campaigns available");
            throw NoAdsAvailableException.forPlacement(
                command.getPlacementId()
            );
        }
        log.info("Found {} active campaigns", activeCampaigns.size());

        // 3. 사용자 컨텍스트를 DTO로 변환
        UserContextDto userContext = convertUserContext(
            command.getUserContext()
        );

        // 4. 각 캠페인에 대해 타겟팅 매칭 수행
        List<AdCandidate> candidates = activeCampaigns
            .stream()
            .map(campaign -> matchCampaign(campaign, userContext))
            .filter(AdCandidate::isMatched)
            .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            log.warn("No ads matched the targeting criteria");
            throw NoAdsAvailableException.forPlacement(
                command.getPlacementId()
            );
        }
        log.info(
            "{} campaigns matched the targeting criteria",
            candidates.size()
        );

        // 5. 랭킹 스코어로 정렬하여 최고 점수 광고 선택
        AdCandidate selectedCandidate = candidates
            .stream()
            .max(Comparator.comparingLong(AdCandidate::getRankingScore))
            .orElseThrow(() ->
                NoAdsAvailableException.forPlacement(command.getPlacementId())
            );

        log.info(
            "Selected ad from campaign: {}, rankingScore: {}",
            selectedCandidate.getCampaignId(),
            selectedCandidate.getRankingScore()
        );

        // 6. AdSelection 도메인 객체 생성
        AdSelection adSelection = createAdSelection(
            selectedCandidate,
            placement
        );

        // 7. 결과 반환
        return AdSelectionResult.from(adSelection);
    }

    /**
     * 게재 위치 검증
     */
    private Placement validatePlacement(String placementId) {
        Placement placement = placementRepository
            .findById(placementId)
            .orElseThrow(() -> PlacementNotFoundException.withId(placementId));

        if (!placement.canServeAds()) {
            throw InactivePlacementException.withStatus(
                placementId,
                placement.getStatus().name()
            );
        }

        return placement;
    }

    /**
     * 캠페인 매칭 수행
     */
    private AdCandidate matchCampaign(
        CampaignDto campaign,
        UserContextDto userContext
    ) {
        TargetingMatchDto matchResult = targetingClient.matchTargeting(
            campaign.getId(),
            userContext
        );

        return AdCandidate.builder()
            .campaignId(campaign.getId())
            .bid(campaign.getBidAmount())
            .matchScore(matchResult.getMatchScore())
            .matched(matchResult.isMatched() && matchResult.getMatchScore() > 0)
            .build();
    }

    /**
     * 사용자 컨텍스트 변환
     */
    private UserContextDto convertUserContext(
        SelectAdCommand.UserContext context
    ) {
        return UserContextDto.builder()
            .userId(context.getUserId())
            .age(context.getAge())
            .gender(context.getGender())
            .country(context.getCountry())
            .city(context.getCity())
            .deviceType(context.getDeviceType())
            .keywords(context.getKeywords())
            .build();
    }

    /**
     * AdSelection 도메인 객체 생성
     */
    private AdSelection createAdSelection(
        AdCandidate candidate,
        Placement placement
    ) {
        // 실제로는 Campaign Service에서 Ad 정보를 가져와야 하지만,
        // 여기서는 간단하게 캠페인 ID를 기반으로 생성
        SelectedAd selectedAd = SelectedAd.of(
            candidate.getCampaignId(),
            "adgroup-" + candidate.getCampaignId(),
            "ad-" + candidate.getCampaignId()
        );

        // 예상 비용 계산 (가격 모델에 따라 다르게 계산 가능)
        long estimatedCost = calculateEstimatedCost(
            candidate.getBid(),
            placement
        );

        // 노출 토큰 생성
        String impressionToken = generateImpressionToken(
            candidate.getCampaignId()
        );

        return AdSelection.of(
            selectedAd,
            candidate.getMatchScore(),
            candidate.getBid(),
            estimatedCost,
            impressionToken
        );
    }

    /**
     * 예상 비용 계산
     */
    private long calculateEstimatedCost(long bid, Placement placement) {
        // CPM의 경우 1회 노출당 비용 = bid / 1000
        // CPC의 경우 클릭당 비용 = bid
        // CPA의 경우 액션당 비용 = bid
        switch (placement.getPricingModel()) {
            case CPM:
                return bid / 1000;
            case CPC:
            case CPA:
            default:
                return bid;
        }
    }

    /**
     * 노출 토큰 생성
     */
    private String generateImpressionToken(String campaignId) {
        return UUID.randomUUID().toString() + "-" + campaignId;
    }

    /**
     * 광고 후보 내부 클래스
     */
    @lombok.Builder
    @lombok.Getter
    private static class AdCandidate {

        private final String campaignId;
        private final long bid;
        private final int matchScore;
        private final boolean matched;

        /**
         * 랭킹 스코어 계산: bid * matchScore / 100
         */
        public long getRankingScore() {
            return (bid * matchScore) / 100;
        }
    }
}
