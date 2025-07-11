package com.braidsbeautyByAngie.controller;

import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.ports.in.ItemProductServiceIn;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.ApiResponse;


@OpenAPIDefinition(
        info = @Info(
                title = "API-ItemProduct",
                version = "1.0",
                description = "ItemProduct management"
        )
)
@RestController
@RequestMapping("/v1/product-service/itemProduct")
@RequiredArgsConstructor
public class ItemProductController {

    private final ItemProductServiceIn productServiceIn;

    @GetMapping(value = "/{itemProductId}")
    public ResponseEntity<ApiResponse> listItemProductById(@PathVariable(name = "itemProductId") Long itemProductId){
        return ResponseEntity.ok(ApiResponse.ok("Item product retrieved successfully",
                productServiceIn.findItemProductByIdIn(itemProductId)));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> listItemProductsByIds(@RequestParam List<Long> ids){
        return ResponseEntity.ok(ApiResponse.ok("List of item products retrieved successfully",
                productServiceIn.listItemProductsByIdsIn(ids)));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> saveItemProduct(@ModelAttribute RequestItemProduct requestItemProduct){
        return new ResponseEntity<>(ApiResponse.create("item saved", productServiceIn.createItemProductIn(requestItemProduct)), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{itemProductId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> updateItemProduct(@PathVariable(name = "itemProductId") Long itemProductId, @ModelAttribute RequestItemProduct requestItemProduct){
        return ResponseEntity.ok(ApiResponse.create("Item product updated",
                productServiceIn.updateItemProductIn(itemProductId, requestItemProduct)));
    }

    @DeleteMapping("/{itemProductId}")
    public ResponseEntity<ApiResponse> deleteItemProduct(@PathVariable(name = "itemProductId") Long itemProductId){
        return ResponseEntity.ok(ApiResponse.ok("Item product deleted",
                productServiceIn.deleteItemProductIn(itemProductId)));
    }

}
