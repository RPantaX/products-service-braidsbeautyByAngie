package com.braidsbeautyByAngie.impl;

import com.braidsbeautyByAngie.aggregates.dto.VariationDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestVariation;
import com.braidsbeautyByAngie.ports.in.VariationServiceIn;
import com.braidsbeautyByAngie.ports.out.VariationServiceOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariationServiceImpl implements VariationServiceIn {

    private final VariationServiceOut variationServiceOut;

    @Override
    public VariationDTO createVariationIn(RequestVariation requestVariation) {
        return variationServiceOut.createVariationOut(requestVariation);
    }

    @Override
    public VariationDTO updateVariationIn(Long variationId, RequestVariation requestVariation) {
        return variationServiceOut.updateVariationOut(variationId, requestVariation);
    }

    @Override
    public VariationDTO deleteVariationIn(Long variationId) {
        return variationServiceOut.deleteVariationOut(variationId);
    }

    @Override
    public VariationDTO findVariationByIdIn(Long variationId) {
        return variationServiceOut.findVariationByIdOut(variationId);
    }

    @Override
    public List<VariationDTO> listVariationIn() {
        return variationServiceOut.listVariationOut();
    }
}
