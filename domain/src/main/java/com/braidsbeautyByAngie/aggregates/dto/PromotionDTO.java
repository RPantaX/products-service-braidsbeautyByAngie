package com.braidsbeautyByAngie.aggregates.dto;

import lombok.*;

import java.security.Timestamp;

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

    private Boolean state;

    private String modifiedByUser;

    private java.sql.Timestamp createdAt;

    private java.sql.Timestamp modifiedAt;

    private java.sql.Timestamp deletedAt;
}
