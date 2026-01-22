package com.adplatform.advertiser.application.usecase;

import com.adplatform.advertiser.application.dto.DeductBalanceCommand;
import com.adplatform.advertiser.domain.exception.AdvertiserNotFoundException;
import com.adplatform.advertiser.domain.model.Advertiser;
import com.adplatform.advertiser.domain.repository.AdvertiserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 잔액 차감 Use Case
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DeductBalanceUseCase {

    private final AdvertiserRepository advertiserRepository;

    public Advertiser execute(DeductBalanceCommand command) {
        // 1. 광고주 조회
        Advertiser advertiser = advertiserRepository.findById(command.getAdvertiserId())
            .orElseThrow(() -> AdvertiserNotFoundException.withId(command.getAdvertiserId()));

        // 2. 잔액 차감 (도메인 로직에서 잔액 부족 검증)
        advertiser.deductBalance(command.getAmount());

        // 3. 저장
        return advertiserRepository.save(advertiser);
    }
}
