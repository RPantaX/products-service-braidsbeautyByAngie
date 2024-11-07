package com.braidsbeautyByAngie.ports.in;


import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseListPageableCategory;

import java.util.Optional;

public interface CategoryServiceIn {

    ProductCategoryDTO createCategoryIn(RequestCategory requestCategory);

    Optional<ResponseCategory> findCategoryByIdIn(Long categoryId);

    ProductCategoryDTO updateCategoryIn(RequestCategory requestCategory, Long categoryId);

    ProductCategoryDTO deleteCategoryIn(Long categoryId);

    ResponseListPageableCategory listCategoryPageableIn(int pageNumber, int pageSize, String orderBy, String sortDir);



}
