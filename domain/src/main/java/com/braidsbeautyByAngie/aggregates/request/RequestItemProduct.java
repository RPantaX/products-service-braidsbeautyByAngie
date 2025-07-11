package com.braidsbeautyByAngie.aggregates.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RequestItemProduct {
    private Long productId;
    private String productItemSKU;
    private int productItemQuantityInStock;
    private MultipartFile imagen;
    private BigDecimal productItemPrice;
    private List<RequestVariationName> requestVariations = new ArrayList<>();
    private boolean deleteFile;
}
