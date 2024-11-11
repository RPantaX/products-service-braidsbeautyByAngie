package com.braidsbeautyByAngie.ports.in;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseListPageableProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProduct;

import java.util.Optional;

public interface ProductServiceIn {

    ProductDTO createProductIn(RequestProduct requestProduct);

    Optional<ResponseProduct> findProductByIdIn(Long productId);

    ProductDTO updateProductIn(Long productId, RequestProduct requestProduct);

    ProductDTO deleteProductIn(Long productId);

    ResponseListPageableProduct listProductPageableIn(int pageNumber, int pageSize, String orderBy, String sortDir);
}
