package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VariationOptionDTO {

    private Long variationOptionId;

    private String variationOptionValue;

    private Boolean state;

    private String modifiedByUser;

    private Timestamp createdAt;

    private Timestamp modifiedAt;

    private Timestamp deletedAt;
}
