package com.braidsbeautyByAngie.aggregates.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RequestProduct {
    private String productName;
    private String productDescription;
    private String productImage;
    private Long productCategoryId;
}
