package com.adplatform.inventory.application.usecase;

import com.adplatform.inventory.application.dto.CreatePlacementCommand;
import com.adplatform.inventory.domain.model.Placement;
import com.adplatform.inventory.domain.repository.PlacementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 게재 위치 생성 유스케이스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreatePlacementUseCase {

    private final PlacementRepository placementRepository;

    @Transactional
    public Placement execute(CreatePlacementCommand command) {
        log.info("Creating placement: name={}, publisherId={}, type={}",
            command.getName(), command.getPublisherId(), command.getPlacementType());

        // 새 게재 위치 생성
        Placement placement = Placement.builder()
            .id(UUID.randomUUID().toString())
            .name(command.getName())
            .publisherId(command.getPublisherId())
            .placementType(command.getPlacementType())
            .pricingModel(command.getPricingModel())
            .basePrice(command.getBasePrice())
            .build();

        // 저장
        Placement saved = placementRepository.save(placement);
        log.info("Placement created: id={}", saved.getId());

        return saved;
    }
}
