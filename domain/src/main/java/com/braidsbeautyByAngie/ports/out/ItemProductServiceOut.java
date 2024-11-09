package com.braidsbeautyByAngie.ports.out;

import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseItemProduct;

import java.util.Optional;

public interface ItemProductServiceOut {
    ProductItemDTO createItemProductOut(RequestItemProduct requestItemProduct);

    Optional<ResponseItemProduct> findItemProductByIdOut(String itemProductId);

    ProductItemDTO updateItemProductOut(String itemProductId, RequestItemProduct requestItemProduct);

    ProductItemDTO deleteItemProductOut(String itemProductId);
}
