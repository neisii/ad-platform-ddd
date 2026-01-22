package com.adplatform.inventory.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Targeting Service 연동 클라이언트
 */
@Component
@Slf4j
public class TargetingClient {

    private final RestTemplate restTemplate;
    private final String targetingServiceUrl;

    public TargetingClient(
        RestTemplate restTemplate,
        @Value("${services.targeting.url:http://localhost:8083}") String targetingServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.targetingServiceUrl = targetingServiceUrl;
    }

    /**
     * 타겟팅 매칭 수행
     */
    public TargetingMatchDto matchTargeting(String campaignId, UserContextDto userContext) {
        try {
            String url = targetingServiceUrl + "/api/v1/targeting/match";
            log.info("Matching targeting for campaign {} at: {}", campaignId, url);

            // 요청 바디 구성
            MatchTargetingRequest request = new MatchTargetingRequest();
            request.setCampaignId(campaignId);
            request.setUserContext(userContext);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<MatchTargetingRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<TargetingMatchDto> response = restTemplate.postForEntity(
                url,
                entity,
                TargetingMatchDto.class
            );

            TargetingMatchDto result = response.getBody();
            if (result != null) {
                log.info("Targeting match result for campaign {}: score={}, matched={}",
                    campaignId, result.getMatchScore(), result.isMatched());
            }
            return result;

        } catch (Exception e) {
            log.error("Failed to match targeting for campaign: {}", campaignId, e);
            // 실패 시 기본값 반환 (스코어 0, 매칭 안됨)
            return TargetingMatchDto.builder()
                .campaignId(campaignId)
                .matchScore(0)
                .matched(false)
                .build();
        }
    }

    /**
     * 타겟팅 매칭 요청 DTO
     */
    @lombok.Data
    private static class MatchTargetingRequest {
        private String campaignId;
        private UserContextDto userContext;
    }
}
