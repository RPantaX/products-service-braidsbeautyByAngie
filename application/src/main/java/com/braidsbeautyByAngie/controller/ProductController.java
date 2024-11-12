package com.braidsbeautyByAngie.controller;

import com.braidsbeautyByAngie.aggregates.constants.Constants;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseListPageableProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProduct;
import com.braidsbeautyByAngie.ports.in.ProductServiceIn;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
@OpenAPIDefinition(
        info = @Info(
                title = "API-PRODUCT",
                version = "1.0",
                description = "Product management"
        )
)
@RestController
@RequestMapping("/v1/product-service/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductServiceIn productServiceIn;

    @GetMapping("/list")
    public ResponseEntity<ResponseListPageableProduct> listProductPageableList(@RequestParam(value = "pageNo", defaultValue = Constants.NUM_PAG_BY_DEFECT, required = false) int pageNo,
                                                                                @RequestParam(value = "pageSize", defaultValue = Constants.SIZE_PAG_BY_DEFECT, required = false) int pageSize,
                                                                                @RequestParam(value = "sortBy", defaultValue = Constants.ORDER_BY_DEFECT_ALL, required = false) String sortBy,
                                                                                @RequestParam(value = "sortDir", defaultValue = Constants.ORDER_DIRECT_BY_DEFECT, required = false) String sortDir){
        return ResponseEntity.ok(productServiceIn.listProductPageableIn(pageNo, pageSize, sortBy, sortDir));
    }

    @GetMapping(value = "/{productId}")
    public ResponseEntity<Optional<ResponseProduct>> listProductById(@PathVariable(name = "productId") Long productId){
        return ResponseEntity.ok(productServiceIn.findProductByIdIn(productId));
    }
    @PostMapping()
    public ResponseEntity<ProductDTO> saveProduct(@RequestBody RequestProduct requestProduct){
        return new ResponseEntity<>(productServiceIn.createProductIn(requestProduct), HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable(name = "productId") Long productId, @RequestBody RequestProduct requestProduct){
        return ResponseEntity.ok(productServiceIn.updateProductIn(productId, requestProduct));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable(name = "productId") Long productId){
        return ResponseEntity.ok(productServiceIn.deleteProductIn(productId));
    }


}
