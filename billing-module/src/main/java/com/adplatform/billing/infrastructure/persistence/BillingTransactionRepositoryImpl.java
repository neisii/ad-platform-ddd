package com.adplatform.billing.infrastructure.persistence;

import com.adplatform.billing.domain.model.BillingTransaction;
import com.adplatform.billing.domain.repository.BillingTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BillingTransaction Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class BillingTransactionRepositoryImpl implements BillingTransactionRepository {

    private final BillingTransactionJpaRepository jpaRepository;
    private final BillingTransactionMapper mapper;

    @Override
    public BillingTransaction save(BillingTransaction transaction) {
        BillingTransactionEntity entity = mapper.toEntity(transaction);
        BillingTransactionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<BillingTransaction> findById(String id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<BillingTransaction> findByDailyMetricsId(String dailyMetricsId) {
        return jpaRepository.findByDailyMetricsId(dailyMetricsId)
            .map(mapper::toDomain);
    }

    @Override
    public List<BillingTransaction> findByAdvertiserId(String advertiserId) {
        return jpaRepository.findByAdvertiserIdOrderByBillingDateDesc(advertiserId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<BillingTransaction> findByCampaignId(String campaignId) {
        return jpaRepository.findByCampaignIdOrderByBillingDateDesc(campaignId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<BillingTransaction> findByBillingDateBetween(LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByBillingDateBetweenOrderByBillingDateDesc(startDate, endDate)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<BillingTransaction> findByAdvertiserIdAndBillingDateBetween(
        String advertiserId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByAdvertiserIdAndBillingDateBetweenOrderByBillingDateDesc(
                advertiserId, startDate, endDate)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByDailyMetricsId(String dailyMetricsId) {
        return jpaRepository.existsByDailyMetricsId(dailyMetricsId);
    }
}
