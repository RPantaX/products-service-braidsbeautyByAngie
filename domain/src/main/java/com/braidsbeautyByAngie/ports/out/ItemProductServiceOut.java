package com.braidsbeautyByAngie.ports.out;

import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseItemProduct;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.dto.Product;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.events.ProductReservedEvent;

import java.util.List;
import java.util.Optional;

public interface ItemProductServiceOut {
    ProductItemDTO createItemProductOut(RequestItemProduct requestItemProduct);

    Optional<ResponseItemProduct> findItemProductByIdOut(Long itemProductId);

    ProductItemDTO updateItemProductOut(Long itemProductId, RequestItemProduct requestItemProduct);

    ProductItemDTO deleteItemProductOut(Long itemProductId);

    List<Product> reserveProductOut(Long shopOrderId, List<Product> desiredProducts);

    void cancelProductReservationOut(Long shopOrderId, List<Product> productsToCancel);

}
