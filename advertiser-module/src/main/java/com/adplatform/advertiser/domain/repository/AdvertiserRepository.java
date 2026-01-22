package com.adplatform.advertiser.domain.repository;

import com.adplatform.advertiser.domain.model.Advertiser;

import java.util.Optional;

/**
 * Advertiser Repository Interface
 */
public interface AdvertiserRepository {

    Advertiser save(Advertiser advertiser);

    Optional<Advertiser> findById(String id);

    Optional<Advertiser> findByEmail(String email);

    boolean existsById(String id);

    boolean existsByEmail(String email);

    void deleteById(String id);
}
