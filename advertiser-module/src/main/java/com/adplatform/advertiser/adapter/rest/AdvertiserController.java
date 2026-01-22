package com.adplatform.advertiser.adapter.rest;

import com.adplatform.advertiser.adapter.rest.dto.*;
import com.adplatform.advertiser.application.dto.ChargeBalanceCommand;
import com.adplatform.advertiser.application.dto.CreateAdvertiserCommand;
import com.adplatform.advertiser.application.dto.DeductBalanceCommand;
import com.adplatform.advertiser.application.usecase.ChargeBalanceUseCase;
import com.adplatform.advertiser.application.usecase.CreateAdvertiserUseCase;
import com.adplatform.advertiser.application.usecase.DeductBalanceUseCase;
import com.adplatform.advertiser.domain.model.Advertiser;
import com.adplatform.advertiser.domain.repository.AdvertiserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Advertiser REST Controller
 */
@RestController
@RequestMapping("/api/v1/advertisers")
@RequiredArgsConstructor
public class AdvertiserController {

    private final CreateAdvertiserUseCase createAdvertiserUseCase;
    private final ChargeBalanceUseCase chargeBalanceUseCase;
    private final DeductBalanceUseCase deductBalanceUseCase;
    private final AdvertiserRepository advertiserRepository;

    /**
     * 광고주 생성
     */
    @PostMapping
    public ResponseEntity<AdvertiserResponse> createAdvertiser(
            @Valid @RequestBody CreateAdvertiserRequest request) {

        CreateAdvertiserCommand command = CreateAdvertiserCommand.builder()
            .name(request.getName())
            .email(request.getEmail())
            .build();

        Advertiser advertiser = createAdvertiserUseCase.execute(command);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(AdvertiserResponse.from(advertiser));
    }

    /**
     * 광고주 조회
     */
    @GetMapping("/{advertiserId}")
    public ResponseEntity<AdvertiserResponse> getAdvertiser(
            @PathVariable String advertiserId) {

        return advertiserRepository.findById(advertiserId)
            .map(AdvertiserResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 잔액 충전
     */
    @PostMapping("/{advertiserId}/charge")
    public ResponseEntity<AdvertiserResponse> chargeBalance(
            @PathVariable String advertiserId,
            @Valid @RequestBody ChargeBalanceRequest request) {

        ChargeBalanceCommand command = ChargeBalanceCommand.builder()
            .advertiserId(advertiserId)
            .amount(request.getAmount())
            .build();

        Advertiser advertiser = chargeBalanceUseCase.execute(command);

        return ResponseEntity.ok(AdvertiserResponse.from(advertiser));
    }

    /**
     * 잔액 차감
     */
    @PostMapping("/{advertiserId}/deduct")
    public ResponseEntity<AdvertiserResponse> deductBalance(
            @PathVariable String advertiserId,
            @Valid @RequestBody DeductBalanceRequest request) {

        DeductBalanceCommand command = DeductBalanceCommand.builder()
            .advertiserId(advertiserId)
            .amount(request.getAmount())
            .build();

        Advertiser advertiser = deductBalanceUseCase.execute(command);

        return ResponseEntity.ok(AdvertiserResponse.from(advertiser));
    }

    /**
     * 광고주 존재 여부 확인
     */
    @GetMapping("/{advertiserId}/exists")
    public ResponseEntity<Map<String, Boolean>> checkAdvertiserExists(
            @PathVariable String advertiserId) {

        boolean exists = advertiserRepository.existsById(advertiserId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
