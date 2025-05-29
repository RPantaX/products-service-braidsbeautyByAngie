package com.braidsbeautyByAngie.controller;

import com.braidsbeautyByAngie.aggregates.request.RequestVariation;
import com.braidsbeautyByAngie.ports.in.VariationServiceIn;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.ApiResponse;

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
    public ApiResponse listVariations() {
        return ApiResponse.ok("List of variations retrieved successfully",
                variationServiceIn.listVariationIn());
    }

    @GetMapping(value = "/{variationId}")
    public ApiResponse getVariationById(@PathVariable(name = "variationId") Long variationId) {
        return ApiResponse.ok("Variation retrieved successfully",
                variationServiceIn.findVariationByIdIn(variationId));
    }

    @PostMapping()
    public ApiResponse saveVariation(@RequestBody RequestVariation requestVariation) {
        return ApiResponse.create("Variation saved successfully",
                variationServiceIn.createVariationIn(requestVariation));
    }

    @PutMapping("/{variationId}")
    public ApiResponse updateVariation(@PathVariable(name = "variationId") Long variationId, @RequestBody RequestVariation requestVariation) {
        return ApiResponse.create("Variation updated successfully",
                variationServiceIn.updateVariationIn(variationId, requestVariation));
    }

    @DeleteMapping("/{variationId}")
    public ApiResponse deleteVariation(@PathVariable(name = "variationId") Long variationId) {
        return ApiResponse.ok("Variation deleted successfully",
                variationServiceIn.deleteVariationIn(variationId));
    }

}
