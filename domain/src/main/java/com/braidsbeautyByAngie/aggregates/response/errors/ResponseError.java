package com.braidsbeautyByAngie.aggregates.response.errors;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseError {
    private int status;
    private Date timestamp;
    private String message;
    private String details;
    private String path;
}
