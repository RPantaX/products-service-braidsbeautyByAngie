package com.braidsbeautyByAngie.aggregates.response.promotions;

import com.braidsbeautyByAngie.aggregates.dto.ProductCategoryDTO;
import com.braidsbeautyByAngie.aggregates.dto.PromotionDTO;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponsePromotion {
    private PromotionDTO promotionDTO;
    private List<ProductCategoryDTO> categoryDTOList;
}
