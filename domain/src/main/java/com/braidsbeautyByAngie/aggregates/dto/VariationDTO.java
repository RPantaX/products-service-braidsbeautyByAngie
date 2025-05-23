package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VariationDTO {

    private Long variationId;

    private String variationName;

    List<VariationOptionDTO> variationOptionEntities;
}

