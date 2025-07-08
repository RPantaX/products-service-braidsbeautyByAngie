package com.braidsbeautyByAngie.ports.in;


import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestCategory;
import com.braidsbeautyByAngie.aggregates.request.RequestSubCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseListPageableCategory;

import java.util.List;
import java.util.Optional;

public interface CategoryServiceIn {

    ProductCategoryDTO createCategoryIn(RequestCategory requestCategory);

    ProductCategoryDTO createSubCategoryIn(RequestSubCategory requestSubCategory);

    Optional<ResponseCategory> findCategoryByIdIn(Long categoryId);

    ProductCategoryDTO updateCategoryIn(RequestCategory requestCategory, Long categoryId);

    ProductCategoryDTO deleteCategoryIn(Long categoryId);

    ResponseListPageableCategory listCategoryPageableIn(int pageNumber, int pageSize, String orderBy, String sortDir);
    List<ProductCategoryDTO> listCategoryIn();
    ProductCategoryDTO findCategoryByNameIn(String categoryName);
}
