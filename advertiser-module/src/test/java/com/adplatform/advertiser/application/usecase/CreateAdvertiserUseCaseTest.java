package com.adplatform.advertiser.application.usecase;

import com.adplatform.advertiser.application.dto.CreateAdvertiserCommand;
import com.adplatform.advertiser.domain.exception.DuplicateEmailException;
import com.adplatform.advertiser.domain.model.Advertiser;
import com.adplatform.advertiser.domain.model.AdvertiserStatus;
import com.adplatform.advertiser.domain.repository.AdvertiserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAdvertiserUseCaseTest {

    @Mock
    private AdvertiserRepository advertiserRepository;

    private CreateAdvertiserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateAdvertiserUseCase(advertiserRepository);
    }

    @Test
    void 유효한_정보로_광고주를_생성한다() {
        // Given
        when(advertiserRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(advertiserRepository.save(any(Advertiser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CreateAdvertiserCommand command = CreateAdvertiserCommand.builder()
            .name("Test Advertiser")
            .email("test@example.com")
            .build();

        // When
        Advertiser advertiser = useCase.execute(command);

        // Then
        assertThat(advertiser.getName()).isEqualTo("Test Advertiser");
        assertThat(advertiser.getEmail()).isEqualTo("test@example.com");
        assertThat(advertiser.getStatus()).isEqualTo(AdvertiserStatus.ACTIVE);
        assertThat(advertiser.getBalance().getAmount()).isEqualTo(0L);

        verify(advertiserRepository).existsByEmail("test@example.com");
        verify(advertiserRepository).save(any(Advertiser.class));
    }

    @Test
    void 광고주_ID가_자동_생성된다() {
        // Given
        when(advertiserRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(advertiserRepository.save(any(Advertiser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CreateAdvertiserCommand command = CreateAdvertiserCommand.builder()
            .name("Test Advertiser")
            .email("test@example.com")
            .build();

        // When
        Advertiser advertiser = useCase.execute(command);

        // Then
        assertThat(advertiser.getId()).isNotNull();
        assertThat(advertiser.getId()).startsWith("adv-");
    }

    @Test
    void 중복된_이메일로는_광고주를_생성할_수_없다() {
        // Given
        when(advertiserRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        CreateAdvertiserCommand command = CreateAdvertiserCommand.builder()
            .name("Test Advertiser")
            .email("duplicate@example.com")
            .build();

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("이미 사용중인 이메일입니다");

        verify(advertiserRepository, never()).save(any());
    }

    @Test
    void 초기_잔액은_0원이다() {
        // Given
        when(advertiserRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(advertiserRepository.save(any(Advertiser.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CreateAdvertiserCommand command = CreateAdvertiserCommand.builder()
            .name("Test Advertiser")
            .email("test@example.com")
            .build();

        // When
        Advertiser advertiser = useCase.execute(command);

        // Then
        assertThat(advertiser.getBalance().getAmount()).isEqualTo(0L);
        assertThat(advertiser.getBalance().getCurrency()).isEqualTo("KRW");
    }
}
