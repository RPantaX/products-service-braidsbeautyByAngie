package com.braidsbeautyByAngie.aggregates.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RequestCategory {
    private String categoryName;
    private List <Long> promotionListId;

}
