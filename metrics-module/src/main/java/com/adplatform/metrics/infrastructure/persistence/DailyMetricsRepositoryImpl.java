package com.adplatform.metrics.infrastructure.persistence;

import com.adplatform.metrics.domain.model.DailyMetrics;
import com.adplatform.metrics.domain.repository.DailyMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DailyMetrics Repository 구현체
 * - Domain Repository 인터페이스 구현
 * - JPA Repository와 Mapper를 사용하여 영속성 처리
 */
@Repository
@RequiredArgsConstructor
public class DailyMetricsRepositoryImpl implements DailyMetricsRepository {

    private final DailyMetricsJpaRepository jpaRepository;
    private final DailyMetricsMapper mapper;

    @Override
    public DailyMetrics save(DailyMetrics metrics) {
        // Upsert: date + adId로 기존 데이터 확인
        Optional<DailyMetricsEntity> existingEntity =
            jpaRepository.findByDateAndAdId(metrics.getDate(), metrics.getAdId());

        DailyMetricsEntity entity;
        if (existingEntity.isPresent()) {
            // 기존 엔티티 업데이트
            entity = existingEntity.get();
            mapper.updateEntity(entity, metrics);
        } else {
            // 새 엔티티 생성
            entity = mapper.toEntity(metrics);
        }

        DailyMetricsEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<DailyMetrics> findById(String id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<DailyMetrics> findByDateAndAdId(LocalDate date, String adId) {
        return jpaRepository.findByDateAndAdId(date, adId)
            .map(mapper::toDomain);
    }

    @Override
    public List<DailyMetrics> findByAdIdAndDateRange(String adId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByAdIdAndDateRange(adId, startDate, endDate).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<DailyMetrics> findByCampaignIdAndDateRange(String campaignId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByCampaignIdAndDateRange(campaignId, startDate, endDate).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<DailyMetrics> findByAdGroupIdAndDateRange(String adGroupId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByAdGroupIdAndDateRange(adGroupId, startDate, endDate).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<DailyMetrics> findByDate(LocalDate date) {
        return jpaRepository.findByDate(date).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByDateAndAdId(LocalDate date, String adId) {
        return jpaRepository.existsByDateAndAdId(date, adId);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
}
