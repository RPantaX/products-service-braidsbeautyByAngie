package com.braidsbeautyByAngie.aggregates.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RequestProduct {
    private String productName;
    private String productDescription;
    private MultipartFile imagen;
    private Long productCategoryId;
    private boolean deleteFile;
}
