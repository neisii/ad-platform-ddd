package com.adplatform.advertiser.application.usecase;

import com.adplatform.advertiser.application.dto.ChargeBalanceCommand;
import com.adplatform.advertiser.domain.exception.AdvertiserNotFoundException;
import com.adplatform.advertiser.domain.model.Advertiser;
import com.adplatform.advertiser.domain.model.AdvertiserStatus;
import com.adplatform.advertiser.domain.model.Money;
import com.adplatform.advertiser.domain.repository.AdvertiserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeBalanceUseCaseTest {

    @Mock
    private AdvertiserRepository advertiserRepository;

    private ChargeBalanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ChargeBalanceUseCase(advertiserRepository);
    }

    @Test
    void 광고주의_잔액을_충전한다() {
        // Given
        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Advertiser")
            .email("test@example.com")
            .balance(Money.of(10000L))
            .status(AdvertiserStatus.ACTIVE)
            .build();

        when(advertiserRepository.findById("adv-1")).thenReturn(Optional.of(advertiser));
        when(advertiserRepository.save(any(Advertiser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ChargeBalanceCommand command = ChargeBalanceCommand.builder()
            .advertiserId("adv-1")
            .amount(50000L)
            .build();

        // When
        Advertiser result = useCase.execute(command);

        // Then
        assertThat(result.getBalance().getAmount()).isEqualTo(60000L);
        verify(advertiserRepository).findById("adv-1");
        verify(advertiserRepository).save(advertiser);
    }

    @Test
    void 존재하지_않는_광고주는_잔액을_충전할_수_없다() {
        // Given
        when(advertiserRepository.findById("invalid-adv")).thenReturn(Optional.empty());

        ChargeBalanceCommand command = ChargeBalanceCommand.builder()
            .advertiserId("invalid-adv")
            .amount(50000L)
            .build();

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(AdvertiserNotFoundException.class)
            .hasMessageContaining("광고주를 찾을 수 없습니다");

        verify(advertiserRepository, never()).save(any());
    }

    @Test
    void 잔액_충전_후_업데이트_시간이_갱신된다() {
        // Given
        Advertiser advertiser = Advertiser.builder()
            .id("adv-1")
            .name("Test Advertiser")
            .email("test@example.com")
            .balance(Money.of(10000L))
            .status(AdvertiserStatus.ACTIVE)
            .build();

        when(advertiserRepository.findById("adv-1")).thenReturn(Optional.of(advertiser));
        when(advertiserRepository.save(any(Advertiser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ChargeBalanceCommand command = ChargeBalanceCommand.builder()
            .advertiserId("adv-1")
            .amount(50000L)
            .build();

        // When
        Advertiser result = useCase.execute(command);

        // Then
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(advertiserRepository).save(advertiser);
    }
}
