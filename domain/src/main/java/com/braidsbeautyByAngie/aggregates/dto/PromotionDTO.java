package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

import java.sql.Timestamp;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PromotionDTO {

    private Long promotionId;

    private String promotionName;

    private String promotionDescription;

    private Double promotionDiscountRate;

    private Timestamp promotionStartDate;

    private Timestamp promotionEndDate;
}
