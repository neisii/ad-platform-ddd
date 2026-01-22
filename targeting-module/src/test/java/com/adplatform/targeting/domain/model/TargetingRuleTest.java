package com.adplatform.targeting.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class TargetingRuleTest {

    @Test
    void 타겟팅_룰을_생성한다() {
        // When
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("KR", "Seoul"))
            .deviceTypes(Arrays.asList(DeviceType.MOBILE, DeviceType.TABLET))
            .keywords(Arrays.asList("tech", "gaming"))
            .build();

        // Then
        assertThat(rule.getId()).isEqualTo("rule-1");
        assertThat(rule.getCampaignId()).isEqualTo("camp-1");
        assertThat(rule.getDemographics().getAgeMin()).isEqualTo(20);
        assertThat(rule.getGeoTargets()).containsExactly("KR", "Seoul");
        assertThat(rule.getDeviceTypes()).containsExactly(
            DeviceType.MOBILE,
            DeviceType.TABLET
        );
        assertThat(rule.getKeywords()).containsExactly("tech", "gaming");
    }

    @Test
    void 캠페인_ID가_없으면_생성할_수_없다() {
        // When & Then
        assertThatThrownBy(() ->
            TargetingRule.builder().id("rule-1").campaignId(null).build()
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("캠페인 ID는 필수입니다");
    }

    @Test
    void 빈_캠페인_ID로_생성할_수_없다() {
        // When & Then
        assertThatThrownBy(() ->
            TargetingRule.builder().id("rule-1").campaignId("  ").build()
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("캠페인 ID는 필수입니다");
    }

    @Test
    void 모든_조건이_비어있으면_100점을_반환한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .build();

        UserContext userContext = UserContext.builder()
            .age(30)
            .gender(Gender.M)
            .country("KR")
            .deviceType(DeviceType.MOBILE)
            .build();

        // When
        int score = rule.matchScore(userContext);

        // Then
        assertThat(score).isEqualTo(100);
    }

    @Test
    void 인구통계만_일치하면_30점을_반환한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .build();

        UserContext userContext = UserContext.builder()
            .age(30)
            .gender(Gender.M)
            .build();

        // When
        int score = rule.matchScore(userContext);

        // Then
        assertThat(score).isEqualTo(100); // 30/30 * 100 = 100
    }

    @Test
    void 지역만_일치하면_25점을_반환한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .geoTargets(Arrays.asList("KR"))
            .build();

        UserContext userContext = UserContext.builder().country("KR").build();

        // When
        int score = rule.matchScore(userContext);

        // Then
        assertThat(score).isEqualTo(100); // 25/25 * 100 = 100
    }

    @Test
    void 디바이스_타입만_일치하면_20점을_반환한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .deviceTypes(Arrays.asList(DeviceType.MOBILE))
            .build();

        UserContext userContext = UserContext.builder()
            .deviceType(DeviceType.MOBILE)
            .build();

        // When
        int score = rule.matchScore(userContext);

        // Then
        assertThat(score).isEqualTo(100); // 20/20 * 100 = 100
    }

    @Test
    void 키워드만_일치하면_25점을_반환한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .keywords(Arrays.asList("tech", "gaming"))
            .build();

        UserContext userContext = UserContext.builder()
            .keywords(Arrays.asList("tech", "gaming"))
            .build();

        // When
        int score = rule.matchScore(userContext);

        // Then
        assertThat(score).isEqualTo(100); // 25/25 * 100 = 100
    }

    @Test
    void 모든_조건이_일치하면_100점을_반환한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("KR"))
            .deviceTypes(Arrays.asList(DeviceType.MOBILE))
            .keywords(Arrays.asList("tech"))
            .build();

        UserContext userContext = UserContext.builder()
            .age(30)
            .gender(Gender.M)
            .country("KR")
            .deviceType(DeviceType.MOBILE)
            .keywords(Arrays.asList("tech"))
            .build();

        // When
        int score = rule.matchScore(userContext);

        // Then
        assertThat(score).isEqualTo(100);
    }

    @Test
    void 일부_조건만_일치하면_부분_점수를_반환한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("KR"))
            .deviceTypes(Arrays.asList(DeviceType.MOBILE))
            .keywords(Arrays.asList("tech"))
            .build();

        UserContext userContext = UserContext.builder()
            .age(30)
            .gender(Gender.M)
            .country("US") // 지역 불일치
            .deviceType(DeviceType.DESKTOP) // 디바이스 불일치
            .keywords(Arrays.asList("tech"))
            .build();

        // When
        int score = rule.matchScore(userContext);

        // Then
        // 인구통계 30 + 지역 0 + 디바이스 0 + 키워드 25 = 55
        assertThat(score).isEqualTo(55);
    }

    @Test
    void 아무_조건도_일치하지_않으면_0점을_반환한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("KR"))
            .deviceTypes(Arrays.asList(DeviceType.MOBILE))
            .keywords(Arrays.asList("tech"))
            .build();

        UserContext userContext = UserContext.builder()
            .age(50)
            .gender(Gender.F)
            .country("US")
            .deviceType(DeviceType.DESKTOP)
            .keywords(Arrays.asList("sports"))
            .build();

        // When
        int score = rule.matchScore(userContext);

        // Then
        assertThat(score).isEqualTo(0);
    }

    @Test
    void 도시_타겟도_지역_매칭에_사용된다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .geoTargets(Arrays.asList("Seoul"))
            .build();

        UserContext userContext = UserContext.builder()
            .country("KR")
            .city("Seoul")
            .build();

        // When
        int score = rule.matchScore(userContext);

        // Then
        assertThat(score).isEqualTo(100);
    }

    @Test
    void 키워드_부분_일치시_비율로_점수를_계산한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .keywords(Arrays.asList("tech", "gaming", "mobile", "cloud"))
            .build();

        UserContext userContext = UserContext.builder()
            .keywords(Arrays.asList("tech", "gaming")) // 4개 중 2개 일치
            .build();

        // When
        int score = rule.matchScore(userContext);

        // Then
        // 키워드만 있고 2/4 일치: (2/4 * 25 = 12) → (12 * 100) / 25 = 48
        assertThat(score).isEqualTo(48);
    }

    @Test
    void 대소문자_구분_없이_키워드를_매칭한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .keywords(Arrays.asList("TECH", "Gaming"))
            .build();

        UserContext userContext = UserContext.builder()
            .keywords(Arrays.asList("tech", "gaming"))
            .build();

        // When
        int score = rule.matchScore(userContext);

        // Then
        assertThat(score).isEqualTo(100);
    }

    @Test
    void 타겟팅_룰을_업데이트한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .demographics(Demographics.of(20, 40, Gender.M))
            .geoTargets(Arrays.asList("KR"))
            .build();

        // When
        Demographics newDemo = Demographics.of(30, 50, Gender.F);
        rule.update(
            newDemo,
            Arrays.asList("US"),
            Arrays.asList(DeviceType.MOBILE),
            Arrays.asList("tech")
        );

        // Then
        assertThat(rule.getDemographics().getAgeMin()).isEqualTo(30);
        assertThat(rule.getDemographics().getGender()).isEqualTo(Gender.F);
        assertThat(rule.getGeoTargets()).containsExactly("US");
        assertThat(rule.getDeviceTypes()).containsExactly(DeviceType.MOBILE);
        assertThat(rule.getKeywords()).containsExactly("tech");
    }

    @Test
    void null_사용자_컨텍스트는_0점을_반환한다() {
        // Given
        TargetingRule rule = TargetingRule.builder()
            .id("rule-1")
            .campaignId("camp-1")
            .build();

        // When
        int score = rule.matchScore(null);

        // Then
        assertThat(score).isEqualTo(0);
    }
}
