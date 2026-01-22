package com.adplatform.advertiser.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Advertiser JPA Repository
 */
public interface AdvertiserJpaRepository extends JpaRepository<AdvertiserEntity, String> {

    Optional<AdvertiserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
