package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VariationOptionDTO {
    private Long variationOptionId;
    private String variationOptionValue;
    private Long variationId;
}
