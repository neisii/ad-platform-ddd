package com.adplatform.targeting.infrastructure.persistence;

import com.adplatform.targeting.domain.model.DeviceType;
import com.adplatform.targeting.domain.model.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * TargetingRule JPA Entity
 * - 도메인 모델과 분리된 영속성 모델
 */
@Entity
@Table(name = "targeting_rules", indexes = {
    @Index(name = "idx_targeting_campaign_id", columnList = "campaign_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetingRuleEntity {

    @Id
    private String id;

    @Column(name = "campaign_id", nullable = false)
    private String campaignId;

    // Demographics
    @Column(name = "age_min")
    private Integer ageMin;

    @Column(name = "age_max")
    private Integer ageMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    // GeoTargets (JSON 배열로 저장)
    @ElementCollection
    @CollectionTable(name = "targeting_geo_targets",
        joinColumns = @JoinColumn(name = "targeting_rule_id"))
    @Column(name = "geo_target")
    @Builder.Default
    private List<String> geoTargets = new ArrayList<>();

    // DeviceTypes (JSON 배열로 저장)
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "targeting_device_types",
        joinColumns = @JoinColumn(name = "targeting_rule_id"))
    @Column(name = "device_type")
    @Builder.Default
    private List<DeviceType> deviceTypes = new ArrayList<>();

    // Keywords (JSON 배열로 저장)
    @ElementCollection
    @CollectionTable(name = "targeting_keywords",
        joinColumns = @JoinColumn(name = "targeting_rule_id"))
    @Column(name = "keyword")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

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
    public void updateFromDomain(Integer ageMin, Integer ageMax, Gender gender,
                                  List<String> geoTargets, List<DeviceType> deviceTypes,
                                  List<String> keywords) {
        this.ageMin = ageMin;
        this.ageMax = ageMax;
        this.gender = gender;
        this.geoTargets.clear();
        if (geoTargets != null) {
            this.geoTargets.addAll(geoTargets);
        }
        this.deviceTypes.clear();
        if (deviceTypes != null) {
            this.deviceTypes.addAll(deviceTypes);
        }
        this.keywords.clear();
        if (keywords != null) {
            this.keywords.addAll(keywords);
        }
    }
}
