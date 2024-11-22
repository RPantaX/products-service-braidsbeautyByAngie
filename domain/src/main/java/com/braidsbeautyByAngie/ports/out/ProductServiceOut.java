package com.braidsbeautyByAngie.ports.out;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseListPageableProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProduct;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.events.ProductReservedEvent;

import java.util.List;
import java.util.Optional;

public interface ProductServiceOut {

    ProductDTO createProductOut(RequestProduct requestProduct);

    ResponseProduct findProductByIdOut(Long productId);

    ProductDTO updateProductOut(Long productId, RequestProduct requestProduct);

    ProductDTO deleteProductOut(Long productId);

    ResponseListPageableProduct listProductPageableOut(int pageNumber, int pageSize, String orderBy, String sortDir);



}
