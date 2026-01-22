package com.adplatform.inventory.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Campaign Service 연동 클라이언트
 */
@Component
@Slf4j
public class CampaignClient {

    private final RestTemplate restTemplate;
    private final String campaignServiceUrl;

    public CampaignClient(
        RestTemplate restTemplate,
        @Value("${services.campaign.url:http://localhost:8082}") String campaignServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.campaignServiceUrl = campaignServiceUrl;
    }

    /**
     * 활성 캠페인 목록 조회
     */
    public List<CampaignDto> getActiveCampaigns() {
        try {
            String url = campaignServiceUrl + "/api/v1/campaigns/active";
            log.info("Fetching active campaigns from: {}", url);

            ResponseEntity<List<CampaignDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CampaignDto>>() {}
            );

            List<CampaignDto> campaigns = response.getBody();
            log.info("Fetched {} active campaigns", campaigns != null ? campaigns.size() : 0);
            return campaigns != null ? campaigns : Collections.emptyList();

        } catch (Exception e) {
            log.error("Failed to fetch active campaigns", e);
            return Collections.emptyList();
        }
    }

    /**
     * 특정 캠페인 조회
     */
    public CampaignDto getCampaign(String campaignId) {
        try {
            String url = campaignServiceUrl + "/api/v1/campaigns/" + campaignId;
            log.info("Fetching campaign from: {}", url);

            ResponseEntity<CampaignDto> response = restTemplate.getForEntity(url, CampaignDto.class);
            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to fetch campaign: {}", campaignId, e);
            return null;
        }
    }
}
