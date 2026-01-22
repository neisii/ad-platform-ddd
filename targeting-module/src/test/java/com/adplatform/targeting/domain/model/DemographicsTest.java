package com.adplatform.targeting.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DemographicsTest {

    @Test
    void 인구통계_정보를_생성한다() {
        // When
        Demographics demographics = Demographics.of(20, 40, Gender.M);

        // Then
        assertThat(demographics.getAgeMin()).isEqualTo(20);
        assertThat(demographics.getAgeMax()).isEqualTo(40);
        assertThat(demographics.getGender()).isEqualTo(Gender.M);
    }

    @Test
    void 모든_사용자를_타겟하는_인구통계_정보를_생성한다() {
        // When
        Demographics demographics = Demographics.any();

        // Then
        assertThat(demographics.getAgeMin()).isNull();
        assertThat(demographics.getAgeMax()).isNull();
        assertThat(demographics.getGender()).isEqualTo(Gender.ANY);
    }

    @Test
    void 최소_연령이_음수면_생성할_수_없다() {
        // When & Then
        assertThatThrownBy(() -> Demographics.of(-1, 40, Gender.M))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("최소 연령은 0 이상이어야 합니다");
    }

    @Test
    void 최대_연령이_음수면_생성할_수_없다() {
        // When & Then
        assertThatThrownBy(() -> Demographics.of(20, -1, Gender.M))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("최대 연령은 0 이상이어야 합니다");
    }

    @Test
    void 최소_연령이_최대_연령보다_크면_생성할_수_없다() {
        // When & Then
        assertThatThrownBy(() -> Demographics.of(50, 30, Gender.M))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("최소 연령은 최대 연령보다 클 수 없습니다");
    }

    @Test
    void 사용자_연령이_범위_내에_있으면_매칭된다() {
        // Given
        Demographics demographics = Demographics.of(20, 40, Gender.ANY);

        // When & Then
        assertThat(demographics.matchesAge(25)).isTrue();
        assertThat(demographics.matchesAge(20)).isTrue();
        assertThat(demographics.matchesAge(40)).isTrue();
    }

    @Test
    void 사용자_연령이_범위_밖이면_매칭되지_않는다() {
        // Given
        Demographics demographics = Demographics.of(20, 40, Gender.ANY);

        // When & Then
        assertThat(demographics.matchesAge(19)).isFalse();
        assertThat(demographics.matchesAge(41)).isFalse();
    }

    @Test
    void 연령_제한이_없으면_모든_연령과_매칭된다() {
        // Given
        Demographics demographics = Demographics.of(null, null, Gender.ANY);

        // When & Then
        assertThat(demographics.matchesAge(10)).isTrue();
        assertThat(demographics.matchesAge(50)).isTrue();
        assertThat(demographics.matchesAge(100)).isTrue();
    }

    @Test
    void 성별이_일치하면_매칭된다() {
        // Given
        Demographics demographics = Demographics.of(20, 40, Gender.M);

        // When & Then
        assertThat(demographics.matchesGender(Gender.M)).isTrue();
    }

    @Test
    void 성별이_일치하지_않으면_매칭되지_않는다() {
        // Given
        Demographics demographics = Demographics.of(20, 40, Gender.M);

        // When & Then
        assertThat(demographics.matchesGender(Gender.F)).isFalse();
    }

    @Test
    void ANY_성별은_모든_성별과_매칭된다() {
        // Given
        Demographics demographics = Demographics.of(20, 40, Gender.ANY);

        // When & Then
        assertThat(demographics.matchesGender(Gender.M)).isTrue();
        assertThat(demographics.matchesGender(Gender.F)).isTrue();
        assertThat(demographics.matchesGender(Gender.OTHER)).isTrue();
    }

    @Test
    void 연령과_성별_모두_일치하면_매칭된다() {
        // Given
        Demographics demographics = Demographics.of(20, 40, Gender.F);

        // When & Then
        assertThat(demographics.matches(30, Gender.F)).isTrue();
    }

    @Test
    void 연령이나_성별_중_하나라도_일치하지_않으면_매칭되지_않는다() {
        // Given
        Demographics demographics = Demographics.of(20, 40, Gender.F);

        // When & Then
        assertThat(demographics.matches(50, Gender.F)).isFalse();
        assertThat(demographics.matches(30, Gender.M)).isFalse();
    }
}
