package com.braidsbeautyByAngie.adapters;

import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseListPageableCategory;
import com.braidsbeautyByAngie.ports.out.CategoryServiceOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryAdapter implements CategoryServiceOut {
    @Override
    public ProductCategoryDTO createCategoryOut(RequestCategory requestCategory) {
        return null;
    }

    @Override
    public Optional<ResponseCategory> findCategoryByIdOut(Long categoryId) {
        return Optional.empty();
    }

    @Override
    public ProductCategoryDTO updateCategoryOut(RequestCategory requestCategory, Long categoryId) {
        return null;
    }

    @Override
    public ProductCategoryDTO deleteCategoryOut(Long categoryId) {
        return null;
    }

    @Override
    public ResponseListPageableCategory listCategoryPageableOut(int pageNumber, int pageSize, String orderBy, String sortDir) {
        return null;
    }
}
