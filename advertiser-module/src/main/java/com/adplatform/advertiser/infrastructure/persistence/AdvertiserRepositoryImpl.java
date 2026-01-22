package com.adplatform.advertiser.infrastructure.persistence;

import com.adplatform.advertiser.domain.model.Advertiser;
import com.adplatform.advertiser.domain.repository.AdvertiserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Advertiser Repository 구현체
 * - Domain Repository 인터페이스 구현
 * - JPA Repository와 Mapper를 사용하여 영속성 처리
 */
@Repository
@RequiredArgsConstructor
public class AdvertiserRepositoryImpl implements AdvertiserRepository {

    private final AdvertiserJpaRepository jpaRepository;
    private final AdvertiserMapper mapper;

    @Override
    public Advertiser save(Advertiser advertiser) {
        Optional<AdvertiserEntity> existingEntity = jpaRepository.findById(advertiser.getId());

        AdvertiserEntity entity;
        if (existingEntity.isPresent()) {
            // 기존 엔티티 업데이트
            entity = existingEntity.get();
            mapper.updateEntity(entity, advertiser);
        } else {
            // 새 엔티티 생성
            entity = mapper.toEntity(advertiser);
        }

        AdvertiserEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Advertiser> findById(String id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<Advertiser> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
}
