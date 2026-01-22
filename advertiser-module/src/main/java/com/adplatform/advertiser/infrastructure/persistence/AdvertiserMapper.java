package com.adplatform.advertiser.infrastructure.persistence;

import com.adplatform.advertiser.domain.model.Advertiser;
import com.adplatform.advertiser.domain.model.Money;
import org.springframework.stereotype.Component;

/**
 * Domain Model <-> JPA Entity 변환 Mapper
 */
@Component
public class AdvertiserMapper {

    /**
     * Domain Model -> JPA Entity
     */
    public AdvertiserEntity toEntity(Advertiser advertiser) {
        return AdvertiserEntity.builder()
            .id(advertiser.getId())
            .name(advertiser.getName())
            .email(advertiser.getEmail())
            .balance(advertiser.getBalance().getAmount())
            .currency(advertiser.getBalance().getCurrency())
            .status(advertiser.getStatus())
            .build();
    }

    /**
     * JPA Entity -> Domain Model
     */
    public Advertiser toDomain(AdvertiserEntity entity) {
        Money balance = Money.of(entity.getBalance(), entity.getCurrency());

        return Advertiser.builder()
            .id(entity.getId())
            .name(entity.getName())
            .email(entity.getEmail())
            .balance(balance)
            .status(entity.getStatus())
            .build();
    }

    /**
     * 기존 Entity를 Domain 변경사항으로 업데이트
     */
    public void updateEntity(AdvertiserEntity entity, Advertiser advertiser) {
        entity.updateFromDomain(
            advertiser.getName(),
            advertiser.getBalance().getAmount(),
            advertiser.getBalance().getCurrency(),
            advertiser.getStatus()
        );
    }
}
