package com.braidsbeautyByAngie.aggregates.response.products;

import com.braidsbeautyByAngie.aggregates.dto.ProductDTO;
import com.braidsbeautyByAngie.aggregates.dto.ProductItemDTO;
import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import lombok.*;

import java.util.List;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseProduct {
    private ProductItemDTO productItemDTO;
    private ProductDTO productDTO;
    private List<ResponseVariation> responseVariationList;
    private ResponseCategory responseCategory;

}
