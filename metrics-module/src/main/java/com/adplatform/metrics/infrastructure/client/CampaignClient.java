package com.adplatform.metrics.infrastructure.client;

import com.adplatform.metrics.domain.model.PricingModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Campaign 서비스 클라이언트
 * - 캠페인 정보 조회를 위한 외부 서비스 통신
 */
@Component
@RequiredArgsConstructor
public class CampaignClient {

    private final RestTemplate restTemplate;
    private static final String CAMPAIGN_SERVICE_URL = "http://localhost:8082/api/v1/campaigns";

    /**
     * 광고그룹 정보 조회
     */
    public AdGroupDto getAdGroup(String adGroupId) {
        String url = String.format("%s/adgroups/%s", CAMPAIGN_SERVICE_URL, adGroupId);
        return restTemplate.getForObject(url, AdGroupDto.class);
    }

    /**
     * 광고 정보 조회 (광고그룹 정보 포함)
     */
    public AdDto getAd(String adId) {
        String url = String.format("%s/ads/%s", CAMPAIGN_SERVICE_URL, adId);
        return restTemplate.getForObject(url, AdDto.class);
    }

    /**
     * 캠페인 존재 여부 확인
     */
    public boolean campaignExists(String campaignId) {
        try {
            String url = String.format("%s/%s/exists", CAMPAIGN_SERVICE_URL, campaignId);
            Boolean exists = restTemplate.getForObject(url, Boolean.class);
            return exists != null && exists;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * AdGroup DTO
     */
    @Getter
    @Setter
    public static class AdGroupDto {
        private String id;
        private String campaignId;
        private String name;
        private Long bid;
        private String status;
        private PricingModel pricingModel = PricingModel.CPC; // 기본값
    }

    /**
     * Ad DTO
     */
    @Getter
    @Setter
    public static class AdDto {
        private String id;
        private String adGroupId;
        private String campaignId;
        private String name;
        private String status;
        private AdGroupDto adGroup;
    }
}
