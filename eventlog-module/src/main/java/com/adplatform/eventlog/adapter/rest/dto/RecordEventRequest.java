package com.adplatform.eventlog.adapter.rest.dto;

import com.adplatform.eventlog.domain.model.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 이벤트 기록 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordEventRequest {

    @NotBlank(message = "이벤트 ID는 필수입니다")
    private String id;

    @NotNull(message = "이벤트 타입은 필수입니다")
    private EventType eventType;

    @NotBlank(message = "광고 ID는 필수입니다")
    private String adId;

    @NotBlank(message = "캠페인 ID는 필수입니다")
    private String campaignId;

    private String adGroupId;

    private String userId;

    private String sessionId;

    @NotNull(message = "타임스탬프는 필수입니다")
    private Instant timestamp;

    private Map<String, String> metadata;

    private String impressionToken;
}
