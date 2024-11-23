package com.braidsbeautyByAngie.aggregates.response.categories;

import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseListPageableCategory {
    private List<ResponseCategoryPageable> responseCategoryList;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private boolean end;
}
