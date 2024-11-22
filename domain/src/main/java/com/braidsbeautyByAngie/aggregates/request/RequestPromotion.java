package com.braidsbeautyByAngie.aggregates.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RequestPromotion {
    private String promotionName;
    private String promotionDescription;
    private BigDecimal promotionDiscountRate;
    private Timestamp promotionStartDate;
    private Timestamp promotionEndDate;
}
