package com.braidsbeautyByAngie.ports.out;

import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestCategory;
import com.braidsbeautyByAngie.aggregates.request.RequestSubCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseListPageableCategory;

import java.util.List;
import java.util.Optional;

public interface CategoryServiceOut {

    ProductCategoryDTO createCategoryOut(RequestCategory requestCategory);

    ProductCategoryDTO createSubCategoryOut(RequestSubCategory requestSubCategory);

    Optional<ResponseCategory> findCategoryByIdOut(Long categoryId);

    ProductCategoryDTO updateCategoryOut(RequestCategory requestCategory, Long categoryId);

    ProductCategoryDTO deleteCategoryOut(Long categoryId);

    ResponseListPageableCategory listCategoryPageableOut(int pageNumber, int pageSize, String orderBy, String sortDir);
    List<ProductCategoryDTO> listCategoryOut();
    ProductCategoryDTO findCategoryByNameOut(String categoryName);
}
