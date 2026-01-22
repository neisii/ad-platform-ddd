package com.adplatform.inventory.application.usecase;

import com.adplatform.inventory.application.dto.UpdatePlacementCommand;
import com.adplatform.inventory.domain.exception.PlacementNotFoundException;
import com.adplatform.inventory.domain.model.Placement;
import com.adplatform.inventory.domain.repository.PlacementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게재 위치 업데이트 유스케이스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdatePlacementUseCase {

    private final PlacementRepository placementRepository;

    @Transactional
    public Placement execute(UpdatePlacementCommand command) {
        log.info("Updating placement: id={}", command.getPlacementId());

        // 기존 게재 위치 조회
        Placement placement = placementRepository.findById(command.getPlacementId())
            .orElseThrow(() -> PlacementNotFoundException.withId(command.getPlacementId()));

        // 업데이트
        placement.update(
            command.getName(),
            command.getPlacementType(),
            command.getPricingModel(),
            command.getBasePrice()
        );

        // 저장
        Placement updated = placementRepository.save(placement);
        log.info("Placement updated: id={}", updated.getId());

        return updated;
    }
}
