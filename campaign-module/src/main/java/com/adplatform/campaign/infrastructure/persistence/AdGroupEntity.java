package com.adplatform.campaign.infrastructure.persistence;

import com.adplatform.campaign.domain.model.AdStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * AdGroup JPA Entity
 */
@Entity
@Table(name = "ad_groups")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdGroupEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignEntity campaign;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long bid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdStatus status;

    @OneToMany(mappedBy = "adGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AdEntity> ads = new ArrayList<>();

    /**
     * 양방향 관계 설정을 위한 setter
     */
    public void setCampaign(CampaignEntity campaign) {
        this.campaign = campaign;
    }

    /**
     * 도메인 모델 변경사항 동기화
     */
    public void updateFromDomain(String name, Long bid, AdStatus status) {
        this.name = name;
        this.bid = bid;
        this.status = status;
    }

    /**
     * Ad 추가
     */
    public void addAd(AdEntity adEntity) {
        ads.add(adEntity);
        adEntity.setAdGroup(this);
    }
}
