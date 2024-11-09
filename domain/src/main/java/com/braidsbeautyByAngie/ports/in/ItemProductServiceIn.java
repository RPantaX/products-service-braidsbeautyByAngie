package com.braidsbeautyByAngie.ports.in;

import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseItemProduct;

import java.util.Optional;

public interface ItemProductServiceIn {

    ProductItemDTO createItemProductIn(RequestItemProduct requestItemProduct);

    Optional<ResponseItemProduct> findItemProductByIdIn(String itemProductId);

    ProductItemDTO updateItemProductIn(String itemProductId, RequestItemProduct requestItemProduct);

    ProductItemDTO deleteItemProductIn(String itemProductId);


}
