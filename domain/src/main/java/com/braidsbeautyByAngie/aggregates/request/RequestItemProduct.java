package com.braidsbeautyByAngie.aggregates.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RequestItemProduct {
    private Long productId;
    private String productItemSKU;

    private String productItemQuantityInStock;

    private String productItemImage;

    private String productItemPrice;

    private List<Long> promotionId;

    private List <String> variationName;

    private List<String> variationOptionValues;
}
