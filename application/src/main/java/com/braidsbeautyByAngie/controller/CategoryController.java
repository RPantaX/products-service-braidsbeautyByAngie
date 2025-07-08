package com.braidsbeautyByAngie.controller;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.request.RequestCategory;
import com.braidsbeautyByAngie.aggregates.request.RequestSubCategory;
import com.braidsbeautyByAngie.ports.in.CategoryServiceIn;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
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
                description = "Category management"
        )
)
@RestController
@RequestMapping("/v1/product-service/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryServiceIn categoryService;

    @GetMapping("/list/pageable")
    public ResponseEntity<ApiResponse> listCategoryPageableList(@RequestParam(value = "pageNo", defaultValue = Constants.NUM_PAG_BY_DEFECT, required = false) int pageNo,
                                                                                 @RequestParam(value = "pageSize", defaultValue = Constants.SIZE_PAG_BY_DEFECT, required = false) int pageSize,
                                                                                 @RequestParam(value = "sortBy", defaultValue = Constants.ORDER_BY_DEFECT_ALL, required = false) String sortBy,
                                                                                 @RequestParam(value = "sortDir", defaultValue = Constants.ORDER_DIRECT_BY_DEFECT, required = false) String sortDir){
        return ResponseEntity.ok(ApiResponse.ok("List of categories retrieved successfully",
                categoryService.listCategoryPageableIn(pageNo, pageSize, sortBy, sortDir)));
    }
    @GetMapping("/list")
    public ResponseEntity<ApiResponse> listCategory(){
        return ResponseEntity.ok(ApiResponse.ok("List of categories retrieved successfully",
                categoryService.listCategoryIn()));
    }
    @GetMapping(value = "/{categoryId}")
    public ResponseEntity<ApiResponse> listCategoryById(@PathVariable(name = "categoryId") Long categoryId){
        return ResponseEntity.ok(ApiResponse.ok("list category by id",
                categoryService.findCategoryByIdIn(categoryId)));
    }
    @GetMapping(value = "/findByName/{categoryName}")
    public ResponseEntity<ApiResponse> findCategoryByName(@PathVariable(name = "categoryName") String categoryName){
        return ResponseEntity.ok(ApiResponse.ok("list category by name",
                categoryService.findCategoryByNameIn(categoryName)));
    }

    @PostMapping()
    public ResponseEntity<ApiResponse> saveCategory(@RequestBody RequestCategory requestCategory){
        return new ResponseEntity<>(ApiResponse.create("Category saved", categoryService.createCategoryIn(requestCategory)), HttpStatus.CREATED);
    }
    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable(name = "categoryId") Long categoryId,@RequestBody RequestCategory requestCategory){
        return ResponseEntity.ok(ApiResponse.create("Category updated",
                categoryService.updateCategoryIn(requestCategory,categoryId)));
    }
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable(name = "categoryId") Long categoryId){
        return ResponseEntity.ok(ApiResponse.ok("Category deleted",
                categoryService.deleteCategoryIn(categoryId)));
    }
    //subcategories
    @PostMapping("/subcategory")
    public ResponseEntity<ApiResponse> saveSubCategory(@RequestBody RequestSubCategory requestSubCategory){
        return new ResponseEntity<>(ApiResponse.create("Save sub category", categoryService.createSubCategoryIn(requestSubCategory)), HttpStatus.CREATED);
    }

}
