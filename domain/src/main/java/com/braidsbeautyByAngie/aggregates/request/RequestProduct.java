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
public class RequestProduct {
    private String productName;
    private String productDescription;
    private String productImage;
    private Long productCategoryId;
    private List<Long> promotionId;
    private List <String> variationName;
    private List<String> variationOptionValues;
}
