package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductCategoryDTO {

    private Long categoryId;

    private String categoryName;

}
