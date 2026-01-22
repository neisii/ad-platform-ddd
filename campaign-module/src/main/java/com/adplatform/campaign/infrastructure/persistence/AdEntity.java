package com.adplatform.campaign.infrastructure.persistence;

import com.adplatform.campaign.domain.model.AdStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Ad JPA Entity
 */
@Entity
@Table(name = "ads")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_group_id", nullable = false)
    private AdGroupEntity adGroup;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String landingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdStatus status;

    /**
     * 양방향 관계 설정을 위한 setter
     */
    public void setAdGroup(AdGroupEntity adGroup) {
        this.adGroup = adGroup;
    }

    /**
     * 도메인 모델 변경사항 동기화
     */
    public void updateFromDomain(String title, String description, String landingUrl, AdStatus status) {
        this.title = title;
        this.description = description;
        this.landingUrl = landingUrl;
        this.status = status;
    }
}
