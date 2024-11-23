package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductItemDTO {

    private Long productItemId;

    private String productItemSKU;

    private int productItemQuantityInStock;

    private String productItemImage;

    private BigDecimal productItemPrice;

    private Long orderLineId;

    private Long shoppingCartItemId;
}
