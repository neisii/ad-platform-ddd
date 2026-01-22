package com.adplatform.inventory.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 사용자 컨텍스트 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContextDto {
    private String userId;
    private Integer age;
    private String gender;
    private String country;
    private String city;
    private String deviceType;
    private List<String> keywords;
}
