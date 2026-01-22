package com.adplatform.campaign.infrastructure.persistence;

import com.adplatform.campaign.domain.model.AdStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Campaign JPA Entity
 * - 도메인 모델과 분리된 영속성 모델
 */
@Entity
@Table(name = "campaigns")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String advertiserId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long dailyBudget;

    @Column(nullable = false)
    private Long totalBudget;

    @Column(nullable = false)
    private Long spent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AdGroupEntity> adGroups = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * 도메인 모델 변경사항 동기화
     */
    public void updateFromDomain(String name, Long dailyBudget, Long totalBudget,
                                  Long spent, AdStatus status) {
        this.name = name;
        this.dailyBudget = dailyBudget;
        this.totalBudget = totalBudget;
        this.spent = spent;
        this.status = status;
    }

    /**
     * AdGroup 추가
     */
    public void addAdGroup(AdGroupEntity adGroupEntity) {
        adGroups.add(adGroupEntity);
        adGroupEntity.setCampaign(this);
    }
}
