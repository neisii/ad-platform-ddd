package com.adplatform.campaign.adapter.rest;

import com.adplatform.campaign.adapter.rest.dto.AddAdGroupRequest;
import com.adplatform.campaign.adapter.rest.dto.CreateCampaignRequest;
import com.adplatform.campaign.adapter.rest.dto.UpdateCampaignStatusRequest;
import com.adplatform.campaign.application.usecase.AddAdGroupUseCase;
import com.adplatform.campaign.application.usecase.CreateCampaignUseCase;
import com.adplatform.campaign.application.usecase.UpdateCampaignStatusUseCase;
import com.adplatform.campaign.domain.exception.CampaignNotFoundException;
import com.adplatform.campaign.domain.model.*;
import com.adplatform.campaign.domain.repository.CampaignRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignController.class)
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateCampaignUseCase createCampaignUseCase;

    @MockBean
    private UpdateCampaignStatusUseCase updateCampaignStatusUseCase;

    @MockBean
    private AddAdGroupUseCase addAdGroupUseCase;

    @MockBean
    private CampaignRepository campaignRepository;

    @Test
    void 캠페인을_생성한다() throws Exception {
        // Given
        CreateCampaignRequest request = CreateCampaignRequest.builder()
            .advertiserId("adv-1")
            .name("Summer Campaign")
            .dailyBudget(10000L)
            .totalBudget(300000L)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Summer Campaign")
            .budget(new Budget(10000L, 300000L))
            .status(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        when(createCampaignUseCase.execute(any())).thenReturn(campaign);

        // When & Then
        mockMvc.perform(post("/api/v1/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("camp-1"))
            .andExpect(jsonPath("$.name").value("Summer Campaign"))
            .andExpect(jsonPath("$.advertiserId").value("adv-1"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.budget.dailyBudget").value(10000))
            .andExpect(jsonPath("$.budget.totalBudget").value(300000));

        verify(createCampaignUseCase).execute(any());
    }

    @Test
    void 유효성_검증_실패시_400_에러를_반환한다() throws Exception {
        // Given
        CreateCampaignRequest request = CreateCampaignRequest.builder()
            .advertiserId("")  // 빈 문자열
            .name("Summer Campaign")
            .dailyBudget(-1000L)  // 음수
            .totalBudget(300000L)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/campaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다"));

        verify(createCampaignUseCase, never()).execute(any());
    }

    @Test
    void 캠페인을_조회한다() throws Exception {
        // Given
        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Summer Campaign")
            .budget(new Budget(10000L, 300000L))
            .status(AdStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        when(campaignRepository.findById("camp-1")).thenReturn(Optional.of(campaign));

        // When & Then
        mockMvc.perform(get("/api/v1/campaigns/camp-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("camp-1"))
            .andExpect(jsonPath("$.name").value("Summer Campaign"));

        verify(campaignRepository).findById("camp-1");
    }

    @Test
    void 존재하지_않는_캠페인_조회시_404를_반환한다() throws Exception {
        // Given
        when(campaignRepository.findById("invalid-camp")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/campaigns/invalid-camp"))
            .andExpect(status().isNotFound());

        verify(campaignRepository).findById("invalid-camp");
    }

    @Test
    void 캠페인_상태를_변경한다() throws Exception {
        // Given
        UpdateCampaignStatusRequest request = UpdateCampaignStatusRequest.builder()
            .status(AdStatus.PAUSED)
            .build();

        Campaign campaign = Campaign.builder()
            .id("camp-1")
            .advertiserId("adv-1")
            .name("Summer Campaign")
            .budget(new Budget(10000L, 300000L))
            .status(AdStatus.PAUSED)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(30))
            .build();

        when(updateCampaignStatusUseCase.execute(any())).thenReturn(campaign);

        // When & Then
        mockMvc.perform(patch("/api/v1/campaigns/camp-1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PAUSED"));

        verify(updateCampaignStatusUseCase).execute(any());
    }

    @Test
    void 광고그룹을_추가한다() throws Exception {
        // Given
        AddAdGroupRequest request = AddAdGroupRequest.builder()
            .name("Product Category A")
            .bid(500L)
            .build();

        AdGroup adGroup = AdGroup.builder()
            .id("ag-1")
            .campaignId("camp-1")
            .name("Product Category A")
            .bid(500L)
            .status(AdStatus.ACTIVE)
            .build();

        when(addAdGroupUseCase.execute(any())).thenReturn(adGroup);

        // When & Then
        mockMvc.perform(post("/api/v1/campaigns/camp-1/ad-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("ag-1"))
            .andExpect(jsonPath("$.name").value("Product Category A"))
            .andExpect(jsonPath("$.bid").value(500))
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(addAdGroupUseCase).execute(any());
    }
}
