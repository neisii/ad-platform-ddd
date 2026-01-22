package com.adplatform.targeting.adapter.rest;

import com.adplatform.targeting.adapter.rest.dto.CreateTargetingRuleRequest;
import com.adplatform.targeting.adapter.rest.dto.MatchTargetingRequest;
import com.adplatform.targeting.adapter.rest.dto.UpdateTargetingRuleRequest;
import com.adplatform.targeting.application.usecase.CreateTargetingRuleUseCase;
import com.adplatform.targeting.application.usecase.MatchTargetingUseCase;
import com.adplatform.targeting.application.usecase.UpdateTargetingRuleUseCase;
import com.adplatform.targeting.domain.exception.TargetingRuleNotFoundException;
import com.adplatform.targeting.domain.model.*;
import com.adplatform.targeting.domain.repository.TargetingRuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TargetingController.class)
class TargetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateTargetingRuleUseCase createTargetingRuleUseCase;

    @MockBean
    private UpdateTargetingRuleUseCase updateTargetingRuleUseCase;

    @MockBean
    private MatchTargetingUseCase matchTargetingUseCase;

    @MockBean
    private TargetingRuleRepository targetingRuleRepository;

    @Test
    void 타겟팅_룰을_생성한다() throws Exception {
        // Given
        CreateTargetingRuleRequest request = CreateTargetingRuleRequest.builder()
            .campaignId("camp-1")
            .ageMin(20)
            .ageMax(40)
            .gender(Gender.M)
            .geoTargets(Arrays.asList("KR"))
            .deviceTypes(Arrays.asList(DeviceType.MOBILE))
            .keywords(Arrays.asList("tech"))
            .build();

        TargetingRule targetingRule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("KR"))
            .deviceTypes(Arrays.asList(DeviceType.MOBILE))
            .keywords(Arrays.asList("tech"))
            .build();

        when(createTargetingRuleUseCase.execute(any()))
            .thenReturn(targetingRule);

        // When & Then
        mockMvc.perform(post("/api/v1/targeting/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("rule-1"))
            .andExpect(jsonPath("$.campaignId").value("camp-1"))
            .andExpect(jsonPath("$.ageMin").value(20))
            .andExpect(jsonPath("$.ageMax").value(40))
            .andExpect(jsonPath("$.gender").value("M"));

        verify(createTargetingRuleUseCase, times(1)).execute(any());
    }

    @Test
    void 캠페인_ID_없이_타겟팅_룰을_생성할_수_없다() throws Exception {
        // Given
        CreateTargetingRuleRequest request = CreateTargetingRuleRequest.builder()
            .ageMin(20)
            .ageMax(40)
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/targeting/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        verify(createTargetingRuleUseCase, never()).execute(any());
    }

    @Test
    void 타겟팅_룰을_조회한다() throws Exception {
        // Given
        TargetingRule targetingRule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("KR"))
            .build();

        when(targetingRuleRepository.findById("rule-1"))
            .thenReturn(Optional.of(targetingRule));

        // When & Then
        mockMvc.perform(get("/api/v1/targeting/rules/rule-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("rule-1"))
            .andExpect(jsonPath("$.campaignId").value("camp-1"));

        verify(targetingRuleRepository, times(1)).findById("rule-1");
    }

    @Test
    void 존재하지_않는_타겟팅_룰_조회시_404를_반환한다() throws Exception {
        // Given
        when(targetingRuleRepository.findById("non-existent"))
            .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/targeting/rules/non-existent"))
            .andExpect(status().isNotFound());

        verify(targetingRuleRepository, times(1)).findById("non-existent");
    }

    @Test
    void 캠페인별_타겟팅_룰을_조회한다() throws Exception {
        // Given
        TargetingRule rule1 = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.any())
            .build();

        TargetingRule rule2 = TargetingRule.builder()
            .id("rule-2")
            .campaignId("camp-1")
            .demographics(Demographics.any())
            .build();

        when(targetingRuleRepository.findByCampaignId("camp-1"))
            .thenReturn(Arrays.asList(rule1, rule2));

        // When & Then
        mockMvc.perform(get("/api/v1/targeting/rules/campaign/camp-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("rule-1"))
            .andExpect(jsonPath("$[1].id").value("rule-2"));

        verify(targetingRuleRepository, times(1)).findByCampaignId("camp-1");
    }

    @Test
    void 타겟팅_룰을_업데이트한다() throws Exception {
        // Given
        UpdateTargetingRuleRequest request = UpdateTargetingRuleRequest.builder()
            .ageMin(30)
            .ageMax(50)
            .gender(Gender.F)
            .geoTargets(Arrays.asList("US"))
            .deviceTypes(Arrays.asList(DeviceType.DESKTOP))
            .keywords(Arrays.asList("sports"))
            .build();

        TargetingRule updatedRule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(30, 50, Gender.F))
            .geoTargets(Arrays.asList("US"))
            .deviceTypes(Arrays.asList(DeviceType.DESKTOP))
            .keywords(Arrays.asList("sports"))
            .build();

        when(updateTargetingRuleUseCase.execute(any()))
            .thenReturn(updatedRule);

        // When & Then
        mockMvc.perform(put("/api/v1/targeting/rules/rule-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("rule-1"))
            .andExpect(jsonPath("$.ageMin").value(30))
            .andExpect(jsonPath("$.ageMax").value(50))
            .andExpect(jsonPath("$.gender").value("F"));

        verify(updateTargetingRuleUseCase, times(1)).execute(any());
    }

    @Test
    void 타겟팅_매칭을_수행한다() throws Exception {
        // Given
        MatchTargetingRequest request = MatchTargetingRequest.builder()
            .age(30)
            .gender(Gender.M)
            .country("KR")
            .deviceType(DeviceType.MOBILE)
            .keywords(Arrays.asList("tech"))
            .build();

        when(matchTargetingUseCase.execute(any()))
            .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(post("/api/v1/targeting/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

        verify(matchTargetingUseCase, times(1)).execute(any());
    }
}
