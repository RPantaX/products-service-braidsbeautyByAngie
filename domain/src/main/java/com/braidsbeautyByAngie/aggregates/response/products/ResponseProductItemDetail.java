package com.braidsbeautyByAngie.aggregates.response.products;

import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategoryy;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseProductItemDetail {
    private Long productItemId;
    private String productItemSKU;
    private int productItemQuantityInStock;
    private String productItemImage;
    private BigDecimal productItemPrice;
    private ResponseCategoryy responseCategoryy;
    private List<ResponseVariationn> variations;

}