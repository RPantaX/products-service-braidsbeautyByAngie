package com.braidsbeautyByAngie.controller;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestCategory;
import com.braidsbeautyByAngie.aggregates.request.RequestSubCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseListPageableCategory;
import com.braidsbeautyByAngie.ports.in.CategoryServiceIn;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@OpenAPIDefinition(
        info = @Info(
                title = "API-CATEGORY",
                version = "1.0",
                description = "Category management"
        )
)
@RestController
@RequestMapping("/v1/product-service/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryServiceIn categoryService;

    @GetMapping("/list/pageable")
    public ResponseEntity<ResponseListPageableCategory> listCategoryPageableList(@RequestParam(value = "pageNo", defaultValue = Constants.NUM_PAG_BY_DEFECT, required = false) int pageNo,
                                                                                 @RequestParam(value = "pageSize", defaultValue = Constants.SIZE_PAG_BY_DEFECT, required = false) int pageSize,
                                                                                 @RequestParam(value = "sortBy", defaultValue = Constants.ORDER_BY_DEFECT_ALL, required = false) String sortBy,
                                                                                 @RequestParam(value = "sortDir", defaultValue = Constants.ORDER_DIRECT_BY_DEFECT, required = false) String sortDir){
        return ResponseEntity.ok(categoryService.listCategoryPageableIn(pageNo, pageSize, sortBy, sortDir));
    }
    @GetMapping("/list")
    public ResponseEntity<List<ProductCategoryDTO>> listCategory(){
        return ResponseEntity.ok(categoryService.listCategoryIn());
    }
    @GetMapping(value = "/{categoryId}")
    public ResponseEntity<Optional<ResponseCategory>> listCategoryById(@PathVariable(name = "categoryId") Long categoryId){
        return ResponseEntity.ok(categoryService.findCategoryByIdIn(categoryId));
    }

    @PostMapping()
    public ResponseEntity<ProductCategoryDTO> saveCategory(@RequestBody RequestCategory requestCategory){
        return new ResponseEntity<>(categoryService.createCategoryIn(requestCategory), HttpStatus.CREATED);
    }
    @PutMapping("/{categoryId}")
    public ResponseEntity<ProductCategoryDTO> updateCategory(@PathVariable(name = "categoryId") Long categoryId,@RequestBody RequestCategory requestCategory){
        return ResponseEntity.ok(categoryService.updateCategoryIn(requestCategory,categoryId));
    }
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ProductCategoryDTO> deleteCategory(@PathVariable(name = "categoryId") Long categoryId){
        return ResponseEntity.ok(categoryService.deleteCategoryIn(categoryId));
    }
    //subcategories
    @PostMapping("/subcategory")
    public ResponseEntity<ProductCategoryDTO> saveSubCategory(@RequestBody RequestSubCategory requestSubCategory){
        return new ResponseEntity<>(categoryService.createSubCategoryIn(requestSubCategory), HttpStatus.CREATED);
    }

}
