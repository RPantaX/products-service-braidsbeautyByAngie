package com.braidsbeautyByAngie.controller;


import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestPromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponseListPageablePromotion;
import com.braidsbeautyByAngie.aggregates.response.promotions.ResponsePromotion;
import com.braidsbeautyByAngie.ports.in.PromotionServiceIn;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
@OpenAPIDefinition(
        info = @Info(
                title = "API-CATEGORY",
                version = "1.0",
                description = "Promotion management"
        )
)
@RestController
@RequestMapping("/v1/product-service/promotion")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionServiceIn promotionService;
    @Operation(summary = "List all promotions")
    @GetMapping("/list")
    public ResponseEntity<ResponseListPageablePromotion> listPromotionPageableList(@RequestParam(value = "pageNo", defaultValue = Constants.NUM_PAG_BY_DEFECT, required = false) int pageNo,
                                                                             @RequestParam(value = "pageSize", defaultValue = Constants.SIZE_PAG_BY_DEFECT, required = false) int pageSize,
                                                                             @RequestParam(value = "sortBy", defaultValue = Constants.ORDER_BY_DEFECT_ALL, required = false) String sortBy,
                                                                             @RequestParam(value = "sortDir", defaultValue = Constants.ORDER_DIRECT_BY_DEFECT, required = false) String sortDir){
        return ResponseEntity.ok(promotionService.listPromotionByPageIn(pageNo, pageSize, sortBy, sortDir));
    }
    @Operation(summary = "List promotion by id")
    @GetMapping(value = "/{promotionId}")
    public ResponseEntity<Optional<ResponsePromotion>> listPromotionById(@PathVariable(name = "promotionId") Long promotionId){
    return ResponseEntity.ok(promotionService.findPromotionByIdIn(promotionId));
    }
    @Operation(summary = "Save promotion")
    @PostMapping()
    public ResponseEntity<PromotionDTO> savePromotion(@RequestBody RequestPromotion requestPromotion){
        return new ResponseEntity<>(promotionService.createPromotionIn(requestPromotion), HttpStatus.CREATED);
    }
    @Operation(summary = "Update promotion")
    @PutMapping("/{promotionId}")
    public ResponseEntity<PromotionDTO> updatePromotion(@PathVariable(name = "promotionId") Long promotionId,@RequestBody RequestPromotion requestPromotion){
        return ResponseEntity.ok(promotionService.updatePromotionIn(promotionId, requestPromotion));
    }
    @Operation(summary = "Delete promotion")
    @DeleteMapping("/{promotionId}")
    public ResponseEntity<PromotionDTO> deletePromotion(@PathVariable(name = "promotionId") Long promotionId){
        return ResponseEntity.ok(promotionService.deletePromotionIn(promotionId));
    }
}
