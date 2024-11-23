package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VariationDTO {

    private Long variationId;

    private String variationName;
}

