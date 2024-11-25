package com.braidsbeautyByAngie.ports.in;

import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseItemProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProductItemDetail;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.dto.Product;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.events.ProductReservedEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ItemProductServiceIn {

    ProductItemDTO createItemProductIn(RequestItemProduct requestItemProduct);

    ResponseProductItemDetail findItemProductByIdIn(Long itemProductId);

    ProductItemDTO updateItemProductIn(Long itemProductId, RequestItemProduct requestItemProduct);

    ProductItemDTO deleteItemProductIn(Long itemProductId);

    List<Product> reserveProductIn(Long shopOrderId, List<Product> desiredProducts);

    void cancelProductReservationIn(Long shopOrderId, List<Product> productsToCancel);
    List<ResponseProductItemDetail> listItemProductsByIdsIn(List<Long> itemProductIds);
}
