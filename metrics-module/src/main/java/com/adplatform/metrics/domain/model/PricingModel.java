package com.adplatform.metrics.domain.model;

/**
 * 가격 책정 모델
 * - CPM: Cost Per Mille (1000 노출당 비용)
 * - CPC: Cost Per Click (클릭당 비용)
 * - CPA: Cost Per Action (전환당 비용)
 */
public enum PricingModel {
    CPM,  // Cost Per Mille (1000 impressions)
    CPC,  // Cost Per Click
    CPA   // Cost Per Action (Conversion)
}
