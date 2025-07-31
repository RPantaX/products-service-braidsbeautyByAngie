package com.braidsbeautyByAngie.repository.dao;

import com.braidsbeautyByAngie.aggregates.request.RequestProductFilter;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseListPageableProduct;

public interface ProductRepositoryCustom {
     ResponseListPageableProduct filterProducts(RequestProductFilter filter);
}
