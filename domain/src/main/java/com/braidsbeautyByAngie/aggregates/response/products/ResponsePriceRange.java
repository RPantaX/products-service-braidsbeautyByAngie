package com.braidsbeautyByAngie.aggregates.response.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponsePriceRange {
    private BigDecimal min;
    private BigDecimal max;
}