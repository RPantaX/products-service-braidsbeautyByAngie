package com.braidsbeautyByAngie.controller;

import com.braidsbeautyByAngie.aggregates.dto.VariationDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestVariation;
import com.braidsbeautyByAngie.ports.in.VariationServiceIn;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@OpenAPIDefinition(
        info = @Info(
                title = "API-CATEGORY",
                version = "1.0",
                description = "Variations management"
        )
)
@RestController
@RequestMapping("/v1/product-service/variation")
@RequiredArgsConstructor
public class VariationController {

    private final VariationServiceIn variationServiceIn;

    @GetMapping("/list")
    public List<VariationDTO> listVariations() {
        return variationServiceIn.listVariationIn();
    }

    @GetMapping(value = "/{variationId}")
    public VariationDTO getVariationById(@PathVariable(name = "variationId") Long variationId) {
        return variationServiceIn.findVariationByIdIn(variationId);
    }

    @PostMapping()
    public VariationDTO saveVariation(@RequestBody RequestVariation requestVariation) {
        return variationServiceIn.createVariationIn(requestVariation);
    }

    @PutMapping("/{variationId}")
    public VariationDTO updateVariation(@PathVariable(name = "variationId") Long variationId, @RequestBody RequestVariation requestVariation) {
        return variationServiceIn.updateVariationIn(variationId, requestVariation);
    }

    @DeleteMapping("/{variationId}")
    public VariationDTO deleteVariation(@PathVariable(name = "variationId") Long variationId) {
        return variationServiceIn.deleteVariationIn(variationId);
    }

}
