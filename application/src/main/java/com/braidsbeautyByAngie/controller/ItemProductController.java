package com.braidsbeautyByAngie.controller;

import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseItemProduct;
import com.braidsbeautyByAngie.ports.in.ItemProductServiceIn;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
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
    public ResponseEntity<Optional<ResponseItemProduct>> listItemProductById(@PathVariable(name = "itemProductId") Long itemProductId){
        return ResponseEntity.ok(productServiceIn.findItemProductByIdIn(itemProductId));
    }
    @PostMapping()
    public ResponseEntity<ProductItemDTO> saveItemProduct(@RequestBody RequestItemProduct requestItemProduct){
        return new ResponseEntity<>(productServiceIn.createItemProductIn(requestItemProduct), HttpStatus.CREATED);
    }

    @PutMapping("/{itemProductId}")
    public ResponseEntity<ProductItemDTO> updateItemProduct(@PathVariable(name = "itemProductId") Long itemProductId, @RequestBody RequestItemProduct requestItemProduct){
        return ResponseEntity.ok(productServiceIn.updateItemProductIn(itemProductId, requestItemProduct));
    }

    @DeleteMapping("/{itemProductId}")
    public ResponseEntity<ProductItemDTO> deleteItemProduct(@PathVariable(name = "itemProductId") Long itemProductId){
        return ResponseEntity.ok(productServiceIn.deleteItemProductIn(itemProductId));
    }

}
