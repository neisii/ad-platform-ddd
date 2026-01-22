package com.adplatform.advertiser.application.usecase;

import com.adplatform.advertiser.application.dto.CreateAdvertiserCommand;
import com.adplatform.advertiser.domain.exception.DuplicateEmailException;
import com.adplatform.advertiser.domain.model.Advertiser;
import com.adplatform.advertiser.domain.model.AdvertiserStatus;
import com.adplatform.advertiser.domain.model.Money;
import com.adplatform.advertiser.domain.repository.AdvertiserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 광고주 생성 Use Case
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreateAdvertiserUseCase {

    private final AdvertiserRepository advertiserRepository;

    public Advertiser execute(CreateAdvertiserCommand command) {
        // 1. 이메일 중복 확인
        if (advertiserRepository.existsByEmail(command.getEmail())) {
            throw DuplicateEmailException.withEmail(command.getEmail());
        }

        // 2. Advertiser Aggregate 생성
        Advertiser advertiser = Advertiser.builder()
            .id(generateAdvertiserId())
            .name(command.getName())
            .email(command.getEmail())
            .balance(Money.of(0L))
            .status(AdvertiserStatus.ACTIVE)
            .build();

        // 3. 저장
        return advertiserRepository.save(advertiser);
    }

    private String generateAdvertiserId() {
        return "adv-" + UUID.randomUUID().toString();
    }
}
