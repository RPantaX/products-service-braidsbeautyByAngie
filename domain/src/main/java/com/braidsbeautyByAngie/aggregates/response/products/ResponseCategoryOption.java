package com.braidsbeautyByAngie.aggregates.response.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCategoryOption {
    private Long id;
    private String name;
    private Integer productCount;
    private Long parentId;
}
