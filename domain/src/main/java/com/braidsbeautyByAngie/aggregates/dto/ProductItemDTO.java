package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductItemDTO {
    private Long productItemId;
    private String productItemSKU;
    private String productItemQuantityInStock;
    private String productItemImage;
    private String productItemPrice;
    private Long orderLineId;
    private Long shoppingCartItemId;
}
