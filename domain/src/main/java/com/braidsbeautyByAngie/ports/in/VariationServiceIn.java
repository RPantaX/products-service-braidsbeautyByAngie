package com.braidsbeautyByAngie.ports.in;

import com.braidsbeautyByAngie.aggregates.dto.VariationDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestVariation;

import java.util.List;

public interface VariationServiceIn {

    VariationDTO createVariationIn(RequestVariation requestVariation);
    VariationDTO updateVariationIn(Long variationId, RequestVariation requestVariation);
    VariationDTO deleteVariationIn(Long variationId);
    VariationDTO findVariationByIdIn(Long variationId);
    List<VariationDTO> listVariationIn();

}
