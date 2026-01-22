package com.adplatform.advertiser.adapter.rest;

import com.adplatform.advertiser.adapter.rest.dto.ChargeBalanceRequest;
import com.adplatform.advertiser.adapter.rest.dto.CreateAdvertiserRequest;
import com.adplatform.advertiser.adapter.rest.dto.DeductBalanceRequest;
import com.adplatform.advertiser.application.usecase.ChargeBalanceUseCase;
import com.adplatform.advertiser.application.usecase.CreateAdvertiserUseCase;
import com.adplatform.advertiser.application.usecase.DeductBalanceUseCase;
import com.adplatform.advertiser.domain.exception.InsufficientBalanceException;
import com.adplatform.advertiser.domain.model.Advertiser;
import com.adplatform.advertiser.domain.model.AdvertiserStatus;
import com.adplatform.advertiser.domain.model.Money;
import com.adplatform.advertiser.domain.repository.AdvertiserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdvertiserController.class)
class AdvertiserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateAdvertiserUseCase createAdvertiserUseCase;

    @MockBean
    private ChargeBalanceUseCase chargeBalanceUseCase;

    @MockBean
    private DeductBalanceUseCase deductBalanceUseCase;

    @MockBean
    private AdvertiserRepository advertiserRepository;

    @Test
    void 광고주를_생성한다() throws Exception {
        // Given
        CreateAdvertiserRequest request = CreateAdvertiserRequest.builder()
            .name("Test Advertiser")
            .email("test@example.com")
            .build();

        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Advertiser")
            .email("test@example.com")
            .balance(Money.of(0L))
            .status(AdvertiserStatus.ACTIVE)
            .build();

        when(createAdvertiserUseCase.execute(any())).thenReturn(advertiser);

        // When & Then
        mockMvc.perform(post("/api/v1/advertisers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("adv-1"))
            .andExpect(jsonPath("$.name").value("Test Advertiser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.balance.amount").value(0))
            .andExpect(jsonPath("$.balance.currency").value("KRW"));

        verify(createAdvertiserUseCase).execute(any());
    }

    @Test
    void 유효성_검증_실패시_400_에러를_반환한다() throws Exception {
        // Given
        CreateAdvertiserRequest request = CreateAdvertiserRequest.builder()
            .name("")  // 빈 문자열
            .email("invalid-email")  // 잘못된 이메일 형식
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/advertisers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다"));

        verify(createAdvertiserUseCase, never()).execute(any());
    }

    @Test
    void 광고주를_조회한다() throws Exception {
        // Given
        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Advertiser")
            .email("test@example.com")
            .balance(Money.of(100000L))
            .status(AdvertiserStatus.ACTIVE)
            .build();

        when(advertiserRepository.findById("adv-1")).thenReturn(Optional.of(advertiser));

        // When & Then
        mockMvc.perform(get("/api/v1/advertisers/adv-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("adv-1"))
            .andExpect(jsonPath("$.name").value("Test Advertiser"))
            .andExpect(jsonPath("$.balance.amount").value(100000));

        verify(advertiserRepository).findById("adv-1");
    }

    @Test
    void 존재하지_않는_광고주_조회시_404를_반환한다() throws Exception {
        // Given
        when(advertiserRepository.findById("invalid-adv")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/advertisers/invalid-adv"))
            .andExpect(status().isNotFound());

        verify(advertiserRepository).findById("invalid-adv");
    }

    @Test
    void 잔액을_충전한다() throws Exception {
        // Given
        ChargeBalanceRequest request = ChargeBalanceRequest.builder()
            .amount(50000L)
            .build();

        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Advertiser")
            .email("test@example.com")
            .balance(Money.of(150000L))
            .status(AdvertiserStatus.ACTIVE)
            .build();

        when(chargeBalanceUseCase.execute(any())).thenReturn(advertiser);

        // When & Then
        mockMvc.perform(post("/api/v1/advertisers/adv-1/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance.amount").value(150000));

        verify(chargeBalanceUseCase).execute(any());
    }

    @Test
    void 잔액을_차감한다() throws Exception {
        // Given
        DeductBalanceRequest request = DeductBalanceRequest.builder()
            .amount(30000L)
            .build();

        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Advertiser")
            .email("test@example.com")
            .balance(Money.of(70000L))
            .status(AdvertiserStatus.ACTIVE)
            .build();

        when(deductBalanceUseCase.execute(any())).thenReturn(advertiser);

        // When & Then
        mockMvc.perform(post("/api/v1/advertisers/adv-1/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance.amount").value(70000));

        verify(deductBalanceUseCase).execute(any());
    }

    @Test
    void 잔액_부족시_400_에러를_반환한다() throws Exception {
        // Given
        DeductBalanceRequest request = DeductBalanceRequest.builder()
            .amount(200000L)
            .build();

        when(deductBalanceUseCase.execute(any()))
            .thenThrow(InsufficientBalanceException.withAmount(200000L, 100000L));

        // When & Then
        mockMvc.perform(post("/api/v1/advertisers/adv-1/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());

        verify(deductBalanceUseCase).execute(any());
    }

    @Test
    void 광고주_존재_여부를_확인한다() throws Exception {
        // Given
        when(advertiserRepository.existsById("adv-1")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/advertisers/adv-1/exists"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(true));

        verify(advertiserRepository).existsById("adv-1");
    }

    @Test
    void 존재하지_않는_광고주는_false를_반환한다() throws Exception {
        // Given
        when(advertiserRepository.existsById("invalid-adv")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/advertisers/invalid-adv/exists"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(false));

        verify(advertiserRepository).existsById("invalid-adv");
    }

    @Test
    void 충전_금액_유효성_검증_실패시_400_에러를_반환한다() throws Exception {
        // Given
        ChargeBalanceRequest request = ChargeBalanceRequest.builder()
            .amount(-1000L)  // 음수
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/advertisers/adv-1/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다"));

        verify(chargeBalanceUseCase, never()).execute(any());
    }
}
