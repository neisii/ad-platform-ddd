package com.adplatform.eventlog.infrastructure.persistence;

import com.adplatform.eventlog.domain.model.AdEvent;
import com.adplatform.eventlog.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 이벤트 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    private final AdEventJpaRepository jpaRepository;

    @Override
    public AdEvent save(AdEvent event) {
        AdEventEntity entity = AdEventEntity.from(event);
        AdEventEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<AdEvent> findById(String id) {
        return jpaRepository.findById(id)
            .map(AdEventEntity::toDomain);
    }

    @Override
    public List<AdEvent> findByAdId(String adId) {
        return jpaRepository.findByAdId(adId)
            .stream()
            .map(AdEventEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<AdEvent> findByTimeRange(Instant startTime, Instant endTime) {
        return jpaRepository.findByTimestampBetween(startTime, endTime)
            .stream()
            .map(AdEventEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }
}
