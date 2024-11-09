package com.braidsbeautyByAngie.aggregates.response.products;

import com.braidsbeautyByAngie.aggregates.response.categories.ResponseCategory;
import lombok.*;

import java.util.List;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseProduct {
    private List<ResponseItemProduct> responseItemProducts;

}
