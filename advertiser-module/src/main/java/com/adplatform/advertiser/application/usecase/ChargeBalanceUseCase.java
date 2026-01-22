package com.adplatform.advertiser.application.usecase;

import com.adplatform.advertiser.application.dto.ChargeBalanceCommand;
import com.adplatform.advertiser.domain.exception.AdvertiserNotFoundException;
import com.adplatform.advertiser.domain.model.Advertiser;
import com.adplatform.advertiser.domain.repository.AdvertiserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 잔액 충전 Use Case
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChargeBalanceUseCase {

    private final AdvertiserRepository advertiserRepository;

    public Advertiser execute(ChargeBalanceCommand command) {
        // 1. 광고주 조회
        Advertiser advertiser = advertiserRepository.findById(command.getAdvertiserId())
            .orElseThrow(() -> AdvertiserNotFoundException.withId(command.getAdvertiserId()));

        // 2. 잔액 충전
        advertiser.chargeBalance(command.getAmount());

        // 3. 저장
        return advertiserRepository.save(advertiser);
    }
}
