package com.adplatform.inventory.adapter.rest;

import com.adplatform.inventory.adapter.rest.dto.*;
import com.adplatform.inventory.application.dto.AdSelectionResult;
import com.adplatform.inventory.application.usecase.CreatePlacementUseCase;
import com.adplatform.inventory.application.usecase.SelectAdUseCase;
import com.adplatform.inventory.application.usecase.UpdatePlacementUseCase;
import com.adplatform.inventory.domain.exception.NoAdsAvailableException;
import com.adplatform.inventory.domain.exception.PlacementNotFoundException;
import com.adplatform.inventory.domain.model.Placement;
import com.adplatform.inventory.domain.model.PlacementType;
import com.adplatform.inventory.domain.model.PricingModel;
import com.adplatform.inventory.domain.repository.PlacementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
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

@WebMvcTest(InventoryController.class)
@DisplayName("InventoryController 테스트")
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreatePlacementUseCase createPlacementUseCase;

    @MockBean
    private UpdatePlacementUseCase updatePlacementUseCase;

    @MockBean
    private SelectAdUseCase selectAdUseCase;

    @MockBean
    private PlacementRepository placementRepository;

    @Test
    @DisplayName("게재 위치를 생성할 수 있다")
    void createPlacement_Success() throws Exception {
        // given
        CreatePlacementRequest request = CreatePlacementRequest.builder()
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .build();

        Placement placement = Placement.builder()
            .id("placement-1")
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .build();

        when(createPlacementUseCase.execute(any())).thenReturn(placement);

        // when & then
        mockMvc.perform(post("/api/v1/inventory/placements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("placement-1"))
            .andExpect(jsonPath("$.name").value("메인 배너"))
            .andExpect(jsonPath("$.publisherId").value("pub-1"))
            .andExpect(jsonPath("$.placementType").value("BANNER"))
            .andExpect(jsonPath("$.pricingModel").value("CPM"))
            .andExpect(jsonPath("$.basePrice").value(1000));

        verify(createPlacementUseCase, times(1)).execute(any());
    }

    @Test
    @DisplayName("게재 위치를 조회할 수 있다")
    void getPlacement_Success() throws Exception {
        // given
        Placement placement = Placement.builder()
            .id("placement-1")
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .build();

        when(placementRepository.findById("placement-1"))
            .thenReturn(Optional.of(placement));

        // when & then
        mockMvc.perform(get("/api/v1/inventory/placements/placement-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("placement-1"))
            .andExpect(jsonPath("$.name").value("메인 배너"));
    }

    @Test
    @DisplayName("존재하지 않는 게재 위치 조회 시 404를 반환한다")
    void getPlacement_NotFound() throws Exception {
        // given
        when(placementRepository.findById("non-existent"))
            .thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/api/v1/inventory/placements/non-existent"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("게재 위치를 업데이트할 수 있다")
    void updatePlacement_Success() throws Exception {
        // given
        UpdatePlacementRequest request = UpdatePlacementRequest.builder()
            .name("업데이트된 배너")
            .placementType(PlacementType.VIDEO)
            .pricingModel(PricingModel.CPC)
            .basePrice(2000L)
            .build();

        Placement placement = Placement.builder()
            .id("placement-1")
            .name("업데이트된 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.VIDEO)
            .pricingModel(PricingModel.CPC)
            .basePrice(2000L)
            .build();

        when(updatePlacementUseCase.execute(any())).thenReturn(placement);

        // when & then
        mockMvc.perform(put("/api/v1/inventory/placements/placement-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("업데이트된 배너"))
            .andExpect(jsonPath("$.placementType").value("VIDEO"));

        verify(updatePlacementUseCase, times(1)).execute(any());
    }

    @Test
    @DisplayName("광고를 선택할 수 있다")
    void selectAd_Success() throws Exception {
        // given
        SelectAdRequest request = SelectAdRequest.builder()
            .placementId("placement-1")
            .userContext(SelectAdRequest.UserContext.builder()
                .userId("user-1")
                .age(25)
                .gender("M")
                .country("KR")
                .city("Seoul")
                .deviceType("MOBILE")
                .keywords(Arrays.asList("sports", "technology"))
                .build())
            .build();

        AdSelectionResult result = AdSelectionResult.builder()
            .campaignId("campaign-1")
            .adGroupId("adgroup-1")
            .adId("ad-1")
            .matchScore(85)
            .bid(5000L)
            .estimatedCost(250L)
            .impressionToken("token-123")
            .build();

        when(selectAdUseCase.execute(any())).thenReturn(result);

        // when & then
        mockMvc.perform(post("/api/v1/inventory/select-ad")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.campaignId").value("campaign-1"))
            .andExpect(jsonPath("$.adGroupId").value("adgroup-1"))
            .andExpect(jsonPath("$.adId").value("ad-1"))
            .andExpect(jsonPath("$.matchScore").value(85))
            .andExpect(jsonPath("$.bid").value(5000))
            .andExpect(jsonPath("$.estimatedCost").value(250))
            .andExpect(jsonPath("$.impressionToken").value("token-123"));

        verify(selectAdUseCase, times(1)).execute(any());
    }

    @Test
    @DisplayName("선택 가능한 광고가 없으면 404를 반환한다")
    void selectAd_NoAdsAvailable() throws Exception {
        // given
        SelectAdRequest request = SelectAdRequest.builder()
            .placementId("placement-1")
            .userContext(SelectAdRequest.UserContext.builder()
                .userId("user-1")
                .build())
            .build();

        when(selectAdUseCase.execute(any()))
            .thenThrow(NoAdsAvailableException.forPlacement("placement-1"));

        // when & then
        mockMvc.perform(post("/api/v1/inventory/select-ad")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("게재 위치가 존재하지 않으면 404를 반환한다")
    void selectAd_PlacementNotFound() throws Exception {
        // given
        SelectAdRequest request = SelectAdRequest.builder()
            .placementId("non-existent")
            .userContext(SelectAdRequest.UserContext.builder()
                .userId("user-1")
                .build())
            .build();

        when(selectAdUseCase.execute(any()))
            .thenThrow(PlacementNotFoundException.withId("non-existent"));

        // when & then
        mockMvc.perform(post("/api/v1/inventory/select-ad")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("유효성 검증 실패 시 400을 반환한다")
    void createPlacement_ValidationFails() throws Exception {
        // given - 이름이 비어있는 잘못된 요청
        CreatePlacementRequest request = CreatePlacementRequest.builder()
            .name("")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/inventory/placements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }
}
