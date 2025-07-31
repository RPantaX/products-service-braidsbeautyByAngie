package com.braidsbeautyByAngie.controller;

import com.braidsbeautyByAngie.aggregates.constants.Constants;

import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.request.RequestProductFilter;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseListPageableProduct;
import com.braidsbeautyByAngie.auth.RequireRole;
import com.braidsbeautyByAngie.ports.in.ProductServiceIn;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.ApiResponse;

import java.math.BigDecimal;
import java.util.List;


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
    @RequireRole // Solo requiere estar autenticado
    public ResponseEntity<ApiResponse> listProductPageableList(@RequestParam(value = "pageNo", defaultValue = Constants.NUM_PAG_BY_DEFECT, required = false) int pageNo,
                                                                                @RequestParam(value = "pageSize", defaultValue = Constants.SIZE_PAG_BY_DEFECT, required = false) int pageSize,
                                                                                @RequestParam(value = "sortBy", defaultValue = Constants.ORDER_BY_DEFECT_ALL, required = false) String sortBy,
                                                                                @RequestParam(value = "sortDir", defaultValue = Constants.ORDER_DIRECT_BY_DEFECT, required = false) String sortDir){
        return ResponseEntity.ok(ApiResponse.ok("List of products retrieved successfully",
                productServiceIn.listProductPageableIn(pageNo, pageSize, sortBy, sortDir)));
    }

    @GetMapping(value = "/{productId}")
    public ResponseEntity<ApiResponse> listProductById(@PathVariable(name = "productId") Long productId){
        return ResponseEntity.ok(ApiResponse.ok("Product retrieved successfully",
                productServiceIn.findProductByIdIn(productId)));
    }
    @RequireRole("ROLE_ADMIN") // Solo administradores
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> saveProduct(@ModelAttribute RequestProduct requestProduct){
        return new ResponseEntity<>(ApiResponse.create("product saved", productServiceIn.createProductIn(requestProduct)), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireRole(value = {"ROLE_ADMIN", "ROLE_MANAGER"}) // Admin O Manager
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable(name = "productId") Long productId, @ModelAttribute RequestProduct requestProduct){
        return ResponseEntity.ok(ApiResponse.create("Product updated",
                productServiceIn.updateProductIn(productId, requestProduct)));
    }
    @RequireRole(value = {"ROLE_ADMIN", "ROLE_SUPER_ADMIN"}, requireAll = true) // Admin Y Super Admin
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable(name = "productId") Long productId){
        return ResponseEntity.ok(ApiResponse.ok("Product deleted",
                productServiceIn.deleteProductIn(productId)));
    }

    @PostMapping("/filter")
    public ResponseEntity<ApiResponse> filterProducts(
            @RequestBody RequestProductFilter filter) {

        ResponseListPageableProduct response = productServiceIn.filterProductsIn(filter);

        return ResponseEntity.ok(ApiResponse.ok("Products with filter", response) );
    }

    // Método alternativo con parámetros GET para filtros simples
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchProducts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Boolean hasPromotion,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        RequestProductFilter filter = RequestProductFilter.builder()
                .searchTerm(searchTerm)
                .categoryIds(categoryId != null ? List.of(categoryId) : null)
                .minPrice(minPrice != null ? new BigDecimal(minPrice) : null)
                .maxPrice(maxPrice != null ? new BigDecimal(maxPrice) : null)
                .inStock(inStock)
                .hasPromotion(hasPromotion)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        ResponseListPageableProduct response = productServiceIn.filterProductsIn(filter);

        return ResponseEntity.ok(ApiResponse.ok("PRODUCTS WITH SEARCH", response) );
    }
    @GetMapping("/filter-options")
    public ResponseEntity<ApiResponse> getProductFilterOptions() {
        return ResponseEntity.ok(ApiResponse.ok("Filter options retrieved successfully",
                productServiceIn.getProductFilterOptionsIn()));
    }
}
