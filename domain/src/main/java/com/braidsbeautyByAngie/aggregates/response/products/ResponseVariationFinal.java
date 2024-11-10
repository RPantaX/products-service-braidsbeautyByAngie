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
public class ResponseVariationFinal {
    private List<VariationOptionDTO> variationOptionDTOList;
    private VariationDTO variationDTO;
}
