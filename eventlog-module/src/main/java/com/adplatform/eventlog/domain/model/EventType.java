package com.adplatform.eventlog.domain.model;

/**
 * 이벤트 타입 열거형
 * - IMPRESSION: 광고 노출
 * - CLICK: 광고 클릭
 * - CONVERSION: 전환
 */
public enum EventType {
    IMPRESSION,
    CLICK,
    CONVERSION
}
