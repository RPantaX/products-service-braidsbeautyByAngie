package com.braidsbeautyByAngie.impl;

import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseItemProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseProductItemDetail;
import com.braidsbeautyByAngie.ports.in.ItemProductServiceIn;
import com.braidsbeautyByAngie.ports.out.ItemProductServiceOut;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.dto.Product;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.events.ProductReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemProductServiceImpl implements ItemProductServiceIn {

    private final ItemProductServiceOut itemProductServiceOut;

    @Override
    public ProductItemDTO createItemProductIn(RequestItemProduct requestItemProduct) {
        return itemProductServiceOut.createItemProductOut(requestItemProduct);
    }

    @Override
    public ResponseProductItemDetail findItemProductByIdIn(Long itemProductId) {
        return itemProductServiceOut.findItemProductByIdOut(itemProductId);
    }

    @Override
    public ProductItemDTO updateItemProductIn(Long itemProductId, RequestItemProduct requestItemProduct) {
        return itemProductServiceOut.updateItemProductOut(itemProductId, requestItemProduct);
    }

    @Override
    public ProductItemDTO deleteItemProductIn(Long itemProductId) {
        return itemProductServiceOut.deleteItemProductOut(itemProductId);
    }

    @Override
    public List<Product> reserveProductIn(Long shopOrderId, List<Product> desiredProducts) {
        return itemProductServiceOut.reserveProductOut(shopOrderId, desiredProducts);
    }

    @Override
    public void cancelProductReservationIn(Long shopOrderId, List<Product> productsToCancel) {
        itemProductServiceOut.cancelProductReservationOut(shopOrderId, productsToCancel);
    }

    @Override
    public List<ResponseProductItemDetail> listItemProductsByIdsIn(List<Long> itemProductIds) {
        return itemProductServiceOut.listItemProductsByIdsOut(itemProductIds);
    }


}
