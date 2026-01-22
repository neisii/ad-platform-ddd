package com.adplatform.inventory.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Placement 도메인 모델 테스트")
class PlacementTest {

    @Test
    @DisplayName("유효한 정보로 Placement를 생성할 수 있다")
    void createPlacement_Success() {
        // given & when
        Placement placement = Placement.builder()
            .id("placement-1")
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .build();

        // then
        assertThat(placement.getId()).isEqualTo("placement-1");
        assertThat(placement.getName()).isEqualTo("메인 배너");
        assertThat(placement.getPublisherId()).isEqualTo("pub-1");
        assertThat(placement.getPlacementType()).isEqualTo(PlacementType.BANNER);
        assertThat(placement.getPricingModel()).isEqualTo(PricingModel.CPM);
        assertThat(placement.getBasePrice()).isEqualTo(1000L);
        assertThat(placement.getStatus()).isEqualTo(PlacementStatus.ACTIVE);
        assertThat(placement.isActive()).isTrue();
    }

    @Test
    @DisplayName("퍼블리셔 ID가 null이면 예외가 발생한다")
    void createPlacement_NullPublisherId_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() ->
            Placement.builder()
                .id("placement-1")
                .name("메인 배너")
                .publisherId(null)
                .placementType(PlacementType.BANNER)
                .pricingModel(PricingModel.CPM)
                .basePrice(1000L)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("퍼블리셔 ID는 필수입니다");
    }

    @Test
    @DisplayName("이름이 비어있으면 예외가 발생한다")
    void createPlacement_EmptyName_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() ->
            Placement.builder()
                .id("placement-1")
                .name("")
                .publisherId("pub-1")
                .placementType(PlacementType.BANNER)
                .pricingModel(PricingModel.CPM)
                .basePrice(1000L)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("게재 위치 이름은 필수입니다");
    }

    @Test
    @DisplayName("게재 위치 타입이 null이면 예외가 발생한다")
    void createPlacement_NullPlacementType_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() ->
            Placement.builder()
                .id("placement-1")
                .name("메인 배너")
                .publisherId("pub-1")
                .placementType(null)
                .pricingModel(PricingModel.CPM)
                .basePrice(1000L)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("게재 위치 타입은 필수입니다");
    }

    @Test
    @DisplayName("가격 모델이 null이면 예외가 발생한다")
    void createPlacement_NullPricingModel_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() ->
            Placement.builder()
                .id("placement-1")
                .name("메인 배너")
                .publisherId("pub-1")
                .placementType(PlacementType.BANNER)
                .pricingModel(null)
                .basePrice(1000L)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("가격 모델은 필수입니다");
    }

    @Test
    @DisplayName("기본 가격이 음수면 예외가 발생한다")
    void createPlacement_NegativeBasePrice_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() ->
            Placement.builder()
                .id("placement-1")
                .name("메인 배너")
                .publisherId("pub-1")
                .placementType(PlacementType.BANNER)
                .pricingModel(PricingModel.CPM)
                .basePrice(-100L)
                .build()
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("기본 가격은 0 이상이어야 합니다");
    }

    @Test
    @DisplayName("Placement 정보를 업데이트할 수 있다")
    void updatePlacement_Success() {
        // given
        Placement placement = Placement.builder()
            .id("placement-1")
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .build();

        // when
        placement.update("새 배너", PlacementType.VIDEO, PricingModel.CPC, 2000L);

        // then
        assertThat(placement.getName()).isEqualTo("새 배너");
        assertThat(placement.getPlacementType()).isEqualTo(PlacementType.VIDEO);
        assertThat(placement.getPricingModel()).isEqualTo(PricingModel.CPC);
        assertThat(placement.getBasePrice()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("Placement를 일시정지할 수 있다")
    void pausePlacement_Success() {
        // given
        Placement placement = Placement.builder()
            .id("placement-1")
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .build();

        // when
        placement.pause();

        // then
        assertThat(placement.getStatus()).isEqualTo(PlacementStatus.PAUSED);
        assertThat(placement.isActive()).isFalse();
        assertThat(placement.canServeAds()).isFalse();
    }

    @Test
    @DisplayName("Placement를 활성화할 수 있다")
    void activatePlacement_Success() {
        // given
        Placement placement = Placement.builder()
            .id("placement-1")
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .status(PlacementStatus.PAUSED)
            .build();

        // when
        placement.activate();

        // then
        assertThat(placement.getStatus()).isEqualTo(PlacementStatus.ACTIVE);
        assertThat(placement.isActive()).isTrue();
        assertThat(placement.canServeAds()).isTrue();
    }

    @Test
    @DisplayName("Placement를 삭제할 수 있다")
    void deletePlacement_Success() {
        // given
        Placement placement = Placement.builder()
            .id("placement-1")
            .name("메인 배너")
            .publisherId("pub-1")
            .placementType(PlacementType.BANNER)
            .pricingModel(PricingModel.CPM)
            .basePrice(1000L)
            .build();

        // when
        placement.delete();

        // then
        assertThat(placement.getStatus()).isEqualTo(PlacementStatus.DELETED);
        assertThat(placement.isActive()).isFalse();
        assertThat(placement.canServeAds()).isFalse();
    }
}
