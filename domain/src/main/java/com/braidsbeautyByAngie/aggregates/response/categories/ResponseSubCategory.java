package com.braidsbeautyByAngie.aggregates.response.categories;

import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseSubCategory {
    private Long productCategoryId;
    private String productCategoryName;
}
