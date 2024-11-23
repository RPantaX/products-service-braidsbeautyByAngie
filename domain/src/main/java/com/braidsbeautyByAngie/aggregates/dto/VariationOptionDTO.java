package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VariationOptionDTO {
    private Long variationOptionId;
    private String variationOptionValue;
}
