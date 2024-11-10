package com.braidsbeautyByAngie.ports.in;

import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseItemProduct;

import java.util.Optional;

public interface ItemProductServiceIn {

    ProductItemDTO createItemProductIn(RequestItemProduct requestItemProduct);

    Optional<ResponseItemProduct> findItemProductByIdIn(Long itemProductId);

    ProductItemDTO updateItemProductIn(Long itemProductId, RequestItemProduct requestItemProduct);

    ProductItemDTO deleteItemProductIn(Long itemProductId);


}
