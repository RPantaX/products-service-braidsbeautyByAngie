package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

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

    private String productItemPrice;

    private Long orderLineId;

    private Long shoppingCartItemId;

    private Boolean state;

    private String modifiedByUser;

    private Timestamp createdAt;

    private Timestamp modifiedAt;

    private Timestamp deletedAt;
}
