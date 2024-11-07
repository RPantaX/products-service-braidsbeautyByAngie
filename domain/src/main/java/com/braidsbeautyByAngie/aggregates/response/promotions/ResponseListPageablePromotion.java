package com.braidsbeautyByAngie.aggregates.response.promotions;

import lombok.*;

import java.util.List;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseListPageablePromotion {
    private List<ResponsePromotion> responsePromotionList;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private boolean end;
}
