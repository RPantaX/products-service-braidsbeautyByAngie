package com.braidsbeautyByAngie.ports.out;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.request.RequestProductFilter;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseListPageableProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProductFilterOptions;

public interface ProductServiceOut {

    ProductDTO createProductOut(RequestProduct requestProduct);

    ResponseProduct findProductByIdOut(Long productId);

    ProductDTO updateProductOut(Long productId, RequestProduct requestProduct);

    ProductDTO deleteProductOut(Long productId);

    ResponseListPageableProduct listProductPageableOut(int pageNumber, int pageSize, String orderBy, String sortDir);

    ResponseListPageableProduct filterProductsOut(RequestProductFilter filter);
    ResponseProductFilterOptions getProductFilterOptionsOut();
}
