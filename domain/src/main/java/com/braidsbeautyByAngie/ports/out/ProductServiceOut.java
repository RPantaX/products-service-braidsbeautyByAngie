package com.braidsbeautyByAngie.ports.out;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseListPageableProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProduct;

import java.util.Optional;

public interface ProductServiceOut {

    ProductDTO createProductOut(RequestProduct requestProduct);

    Optional<ResponseProduct> findProductByIdOut(String productId);

    ProductDTO updateProductOut(String productId, RequestProduct requestProduct);

    ProductDTO deleteProductOut(String productId);

    ResponseListPageableProduct listProductPageableOut(int pageNumber, int pageSize, String orderBy, String sortDir);
}
