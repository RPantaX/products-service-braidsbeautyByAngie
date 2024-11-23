package com.braidsbeautyByAngie.aggregates.response.categories;

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
public class ResponseCategoryPageable {
    private Long productCategoryId;
    private String productCategoryName;
    private List<ResponseSubCategory> responseSubCategoryList;
    private Set<PromotionDTO> promotionDTOList;
    private List<ProductDTO> productDTOList;
}
