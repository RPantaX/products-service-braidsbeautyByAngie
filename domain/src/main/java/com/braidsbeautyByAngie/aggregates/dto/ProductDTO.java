package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductDTO {

    private Long productId;

    private String productName;

    private String productDescription;

    private String productImage;

    private Boolean state;

    private String modifiedByUser;

    private Timestamp createdAt;

    private Timestamp modifiedAt;

    private Timestamp deletedAt;
}
