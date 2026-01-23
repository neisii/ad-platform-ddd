package com.adplatform.metrics.adapter.rest.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 메트릭스 집계 요청 DTO
 */
@Getter
@Setter
public class AggregateMetricsRequest {
    private LocalDate date;
    private LocalDate startDate;
    private LocalDate endDate;
}
