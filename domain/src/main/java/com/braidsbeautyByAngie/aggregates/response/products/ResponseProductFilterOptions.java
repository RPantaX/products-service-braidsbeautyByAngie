package com.braidsbeautyByAngie.aggregates.response.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseProductFilterOptions {
    private List<ResponseCategoryOption> categories;
    private ResponsePriceRange priceRange;
    private List<ResponseVariationOption> variations;
}
