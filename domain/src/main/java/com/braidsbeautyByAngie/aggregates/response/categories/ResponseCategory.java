package com.braidsbeautyByAngie.aggregates.response.categories;

import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import lombok.*;

import java.util.List;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseCategory {
    private ProductCategoryDTO productCategoryDTO;
    private List<ResponseSubCategory> responseSubCategoryList;
    private Set<PromotionDTO> promotionDTOList;
    private List<ProductDTO> productDTOList;
}
