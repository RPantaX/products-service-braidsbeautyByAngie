package com.braidsbeautyByAngie.ports.out;

import com.braidsbeautyByAngie.aggregates.dto.VariationDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestVariation;

import java.util.List;

public interface VariationServiceOut {

    VariationDTO createVariationOut(RequestVariation requestVariation);
    VariationDTO updateVariationOut(Long variationId, RequestVariation requestVariation);
    VariationDTO deleteVariationOut(Long variationId);
    VariationDTO findVariationByIdOut(Long variationId);
    List<VariationDTO> listVariationOut();
}
