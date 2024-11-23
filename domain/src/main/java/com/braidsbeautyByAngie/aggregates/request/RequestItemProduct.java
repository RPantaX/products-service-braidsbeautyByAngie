package com.braidsbeautyByAngie.aggregates.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RequestItemProduct {
    private Long productId;
    private String productItemSKU;
    private int productItemQuantityInStock;
    private String productItemImage;
    private BigDecimal productItemPrice;
    private List<RequestVariationName> requestVariations;
}
