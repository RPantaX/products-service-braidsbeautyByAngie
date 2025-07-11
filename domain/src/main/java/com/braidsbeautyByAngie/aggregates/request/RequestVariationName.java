package com.braidsbeautyByAngie.aggregates.request;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RequestVariationName {
    private String variationName;
    private String variationOptionValue;
}
