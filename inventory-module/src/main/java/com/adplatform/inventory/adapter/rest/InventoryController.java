package com.adplatform.inventory.adapter.rest;

import com.adplatform.inventory.adapter.rest.dto.*;
import com.adplatform.inventory.application.dto.*;
import com.adplatform.inventory.application.usecase.CreatePlacementUseCase;
import com.adplatform.inventory.application.usecase.SelectAdUseCase;
import com.adplatform.inventory.application.usecase.UpdatePlacementUseCase;
import com.adplatform.inventory.domain.model.Placement;
import com.adplatform.inventory.domain.repository.PlacementRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Inventory REST Controller
 * - 게재 위치 관리 API
 * - 광고 선택 API
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final CreatePlacementUseCase createPlacementUseCase;
    private final UpdatePlacementUseCase updatePlacementUseCase;
    private final SelectAdUseCase selectAdUseCase;
    private final PlacementRepository placementRepository;

    /**
     * 게재 위치 생성
     * POST /api/v1/inventory/placements
     */
    @PostMapping("/placements")
    public ResponseEntity<PlacementResponse> createPlacement(
        @Valid @RequestBody CreatePlacementRequest request
    ) {
        log.info("Creating placement: {}", request.getName());

        CreatePlacementCommand command = CreatePlacementCommand.builder()
            .name(request.getName())
            .publisherId(request.getPublisherId())
            .placementType(request.getPlacementType())
            .pricingModel(request.getPricingModel())
            .basePrice(request.getBasePrice())
            .build();

        Placement placement = createPlacementUseCase.execute(command);
        PlacementResponse response = PlacementResponse.from(placement);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 게재 위치 조회
     * GET /api/v1/inventory/placements/{id}
     */
    @GetMapping("/placements/{id}")
    public ResponseEntity<PlacementResponse> getPlacement(@PathVariable String id) {
        log.info("Getting placement: {}", id);

        return placementRepository.findById(id)
            .map(PlacementResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 게재 위치 업데이트
     * PUT /api/v1/inventory/placements/{id}
     */
    @PutMapping("/placements/{id}")
    public ResponseEntity<PlacementResponse> updatePlacement(
        @PathVariable String id,
        @Valid @RequestBody UpdatePlacementRequest request
    ) {
        log.info("Updating placement: {}", id);

        UpdatePlacementCommand command = UpdatePlacementCommand.builder()
            .placementId(id)
            .name(request.getName())
            .placementType(request.getPlacementType())
            .pricingModel(request.getPricingModel())
            .basePrice(request.getBasePrice())
            .build();

        Placement placement = updatePlacementUseCase.execute(command);
        PlacementResponse response = PlacementResponse.from(placement);

        return ResponseEntity.ok(response);
    }

    /**
     * 광고 선택 (핵심 엔드포인트)
     * POST /api/v1/inventory/select-ad
     */
    @PostMapping("/select-ad")
    public ResponseEntity<AdSelectionResponse> selectAd(
        @Valid @RequestBody SelectAdRequest request
    ) {
        log.info("Selecting ad for placement: {}", request.getPlacementId());

        SelectAdCommand command = SelectAdCommand.builder()
            .placementId(request.getPlacementId())
            .userContext(convertUserContext(request.getUserContext()))
            .build();

        AdSelectionResult result = selectAdUseCase.execute(command);
        AdSelectionResponse response = AdSelectionResponse.from(result);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 컨텍스트 변환
     */
    private SelectAdCommand.UserContext convertUserContext(SelectAdRequest.UserContext dto) {
        return SelectAdCommand.UserContext.builder()
            .userId(dto.getUserId())
            .age(dto.getAge())
            .gender(dto.getGender())
            .country(dto.getCountry())
            .city(dto.getCity())
            .deviceType(dto.getDeviceType())
            .keywords(dto.getKeywords())
            .build();
    }
}
