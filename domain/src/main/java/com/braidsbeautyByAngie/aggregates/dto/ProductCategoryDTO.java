package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductCategoryDTO {
    private Long productCategoryId;
    private Long productCategoryParentId;
    private String productCategoryName;
}
