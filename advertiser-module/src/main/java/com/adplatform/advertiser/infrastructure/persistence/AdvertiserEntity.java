package com.adplatform.advertiser.infrastructure.persistence;

import com.adplatform.advertiser.domain.model.AdvertiserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Advertiser JPA Entity
 * - 도메인 모델과 분리된 영속성 모델
 */
@Entity
@Table(name = "advertisers", indexes = {
    @Index(name = "idx_advertiser_email", columnList = "email", unique = true)
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertiserEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Long balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdvertiserStatus status;

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
    public void updateFromDomain(String name, Long balance, String currency, AdvertiserStatus status) {
        this.name = name;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
    }
}
