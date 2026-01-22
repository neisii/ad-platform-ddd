package com.adplatform.targeting.adapter.rest;

import com.adplatform.targeting.adapter.rest.dto.*;
import com.adplatform.targeting.application.dto.CreateTargetingRuleCommand;
import com.adplatform.targeting.application.dto.MatchTargetingCommand;
import com.adplatform.targeting.application.dto.TargetingMatchResult;
import com.adplatform.targeting.application.dto.UpdateTargetingRuleCommand;
import com.adplatform.targeting.application.usecase.CreateTargetingRuleUseCase;
import com.adplatform.targeting.application.usecase.MatchTargetingUseCase;
import com.adplatform.targeting.application.usecase.UpdateTargetingRuleUseCase;
import com.adplatform.targeting.domain.model.TargetingRule;
import com.adplatform.targeting.domain.repository.TargetingRuleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Targeting REST Controller
 */
@RestController
@RequestMapping("/api/v1/targeting")
@RequiredArgsConstructor
public class TargetingController {

    private final CreateTargetingRuleUseCase createTargetingRuleUseCase;
    private final UpdateTargetingRuleUseCase updateTargetingRuleUseCase;
    private final MatchTargetingUseCase matchTargetingUseCase;
    private final TargetingRuleRepository targetingRuleRepository;

    /**
     * 타겟팅 룰 생성
     */
    @PostMapping("/rules")
    public ResponseEntity<TargetingRuleResponse> createTargetingRule(
            @Valid @RequestBody CreateTargetingRuleRequest request) {

        CreateTargetingRuleCommand command = CreateTargetingRuleCommand.builder()
            .campaignId(request.getCampaignId())
            .ageMin(request.getAgeMin())
            .ageMax(request.getAgeMax())
            .gender(request.getGender())
            .geoTargets(request.getGeoTargets())
            .deviceTypes(request.getDeviceTypes())
            .keywords(request.getKeywords())
            .build();

        TargetingRule targetingRule = createTargetingRuleUseCase.execute(command);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(TargetingRuleResponse.from(targetingRule));
    }

    /**
     * 타겟팅 룰 조회
     */
    @GetMapping("/rules/{id}")
    public ResponseEntity<TargetingRuleResponse> getTargetingRule(
            @PathVariable String id) {

        return targetingRuleRepository.findById(id)
            .map(TargetingRuleResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 캠페인별 타겟팅 룰 조회
     */
    @GetMapping("/rules/campaign/{campaignId}")
    public ResponseEntity<List<TargetingRuleResponse>> getTargetingRulesByCampaign(
            @PathVariable String campaignId) {

        List<TargetingRule> rules = targetingRuleRepository.findByCampaignId(campaignId);

        List<TargetingRuleResponse> responses = rules.stream()
            .map(TargetingRuleResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 타겟팅 룰 업데이트
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<TargetingRuleResponse> updateTargetingRule(
            @PathVariable String id,
            @Valid @RequestBody UpdateTargetingRuleRequest request) {

        UpdateTargetingRuleCommand command = UpdateTargetingRuleCommand.builder()
            .targetingRuleId(id)
            .ageMin(request.getAgeMin())
            .ageMax(request.getAgeMax())
            .gender(request.getGender())
            .geoTargets(request.getGeoTargets())
            .deviceTypes(request.getDeviceTypes())
            .keywords(request.getKeywords())
            .build();

        TargetingRule targetingRule = updateTargetingRuleUseCase.execute(command);

        return ResponseEntity.ok(TargetingRuleResponse.from(targetingRule));
    }

    /**
     * 타겟팅 매칭
     */
    @PostMapping("/match")
    public ResponseEntity<List<TargetingMatchResponse>> matchTargeting(
            @Valid @RequestBody MatchTargetingRequest request) {

        MatchTargetingCommand command = MatchTargetingCommand.builder()
            .age(request.getAge())
            .gender(request.getGender())
            .country(request.getCountry())
            .city(request.getCity())
            .deviceType(request.getDeviceType())
            .keywords(request.getKeywords())
            .build();

        List<TargetingMatchResult> results = matchTargetingUseCase.execute(command);

        List<TargetingMatchResponse> responses = results.stream()
            .map(TargetingMatchResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
