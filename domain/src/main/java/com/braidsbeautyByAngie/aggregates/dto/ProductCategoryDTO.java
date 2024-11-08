package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductCategoryDTO {

    private Long productCategoryId;

    private String productCategoryName;

    private Boolean state;

    private String modifiedByUser;

    private Timestamp createdAt;

    private Timestamp modifiedAt;

    private Timestamp deletedAt;

}
