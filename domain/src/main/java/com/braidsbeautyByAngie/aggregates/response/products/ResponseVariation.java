package com.braidsbeautyByAngie.aggregates.response.products;

import com.braidsbeautyByAngie.aggregates.dto.VariationDTO;
import com.braidsbeautyByAngie.aggregates.dto.VariationOptionDTO;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseVariation {
    private VariationOptionDTO variationOptionDTO;
    private List<VariationDTO> variationDTOList;
}
