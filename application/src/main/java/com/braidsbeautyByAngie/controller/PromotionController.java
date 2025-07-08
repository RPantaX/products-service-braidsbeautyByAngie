package com.braidsbeautyByAngie.controller;


import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.request.RequestPromotion;
import com.braidsbeautyByAngie.ports.in.PromotionServiceIn;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.ApiResponse;

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
    @GetMapping("/list/pageable")
    public ResponseEntity<ApiResponse> listPromotionPageableList(@RequestParam(value = "pageNo", defaultValue = Constants.NUM_PAG_BY_DEFECT, required = false) int pageNo,
                                                                             @RequestParam(value = "pageSize", defaultValue = Constants.SIZE_PAG_BY_DEFECT, required = false) int pageSize,
                                                                             @RequestParam(value = "sortBy", defaultValue = Constants.ORDER_BY_DEFECT_ALL, required = false) String sortBy,
                                                                             @RequestParam(value = "sortDir", defaultValue = Constants.ORDER_DIRECT_BY_DEFECT, required = false) String sortDir){
        return ResponseEntity.ok(ApiResponse.ok("List of promotions retrieved successfully",
                promotionService.listPromotionByPageIn(pageNo, pageSize, sortBy, sortDir)));
    }
    @Operation(summary = "List promotion by id")
    @GetMapping(value = "/{promotionId}")
    public ResponseEntity<ApiResponse> listPromotionById(@PathVariable(name = "promotionId") Long promotionId){
    return ResponseEntity.ok(ApiResponse.ok("Promotion retrieved successfully",
                promotionService.findPromotionByIdIn(promotionId)));
    }

    @GetMapping(value = "/list")
    public ResponseEntity<ApiResponse> listPromotion(){
        return ResponseEntity.ok(ApiResponse.ok("List of promotions retrieved successfully",
                promotionService.listPromotionIn()));
    }
    @GetMapping(value = "/findByName/{promotionName}")
    public ResponseEntity<ApiResponse> findPromotionByName(@PathVariable(name = "promotionName") String promotionName){
        return ResponseEntity.ok(ApiResponse.ok("Promotion retrieved by name",
                promotionService.findPromotionByNameIn(promotionName)));
    }

    @Operation(summary = "Save promotion")
    @PostMapping()
    public ResponseEntity<ApiResponse> savePromotion(@RequestBody RequestPromotion requestPromotion){
        return new ResponseEntity<>(ApiResponse.create("promotion saved", promotionService.createPromotionIn(requestPromotion)), HttpStatus.CREATED);
    }
    @Operation(summary = "Update promotion")
    @PutMapping("/{promotionId}")
    public ResponseEntity<ApiResponse> updatePromotion(@PathVariable(name = "promotionId") Long promotionId,@RequestBody RequestPromotion requestPromotion){
        return ResponseEntity.ok(ApiResponse.create("Promotion updated",
                promotionService.updatePromotionIn(promotionId, requestPromotion)));
    }
    @Operation(summary = "Delete promotion")
    @DeleteMapping("/{promotionId}")
    public ResponseEntity<ApiResponse> deletePromotion(@PathVariable(name = "promotionId") Long promotionId){
        return ResponseEntity.ok(ApiResponse.ok("Promotion deleted",
                promotionService.deletePromotionIn(promotionId)));
    }
}
