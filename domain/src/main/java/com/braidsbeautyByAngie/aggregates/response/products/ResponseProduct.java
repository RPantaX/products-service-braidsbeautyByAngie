package com.braidsbeautyByAngie.aggregates.response.products;

import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategoryy;
import lombok.*;

import java.util.List;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseProduct {
    private Long productId;
    private String productName;
    private String productDescription;
    private String productImage;
    private ResponseCategoryy responseCategory;
    private List<ResponseProductItemDetaill> responseProductItemDetails;
}
