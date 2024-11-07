package com.braidsbeautyByAngie.aggregates.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RequestCategory {
    private String productCategoryName;
    private String productCategoryParentId;
}
