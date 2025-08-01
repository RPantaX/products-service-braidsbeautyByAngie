package com.braidsbeautyByAngie.aggregates.response.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseVariationOption {
    private String name;
    private List<String> options;
}
