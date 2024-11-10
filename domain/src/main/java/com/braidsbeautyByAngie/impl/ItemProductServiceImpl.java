package com.braidsbeautyByAngie.impl;

import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestItemProduct;
import com.braidsbeautyByAngie.aggregates.response.products.ResponseItemProduct;
import com.braidsbeautyByAngie.ports.in.ItemProductServiceIn;
import com.braidsbeautyByAngie.ports.out.ItemProductServiceOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public Optional<ResponseItemProduct> findItemProductByIdIn(Long itemProductId) {
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
}
