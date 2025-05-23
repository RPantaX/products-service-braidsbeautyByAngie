package com.braidsbeautyByAngie.middleware;

import com.braidsbeautyByAngie.aggregates.constants.Constants;
import com.braidsbeautyByAngie.aggregates.response.errors.ResponseError;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.AppExceptions.AppExceptionNotFound;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestMiddleware {

    @ExceptionHandler({AppExceptionNotFound.class})
    private ResponseEntity<ResponseError> handleAppExceptionNotFound(AppExceptionNotFound exception,  HttpServletRequest request) {
        return new ResponseEntity<>(ResponseError.builder()
                                                .message(exception.getMessage())
                                                .timestamp(Constants.getTimestamp())
                                                .path(request.getRequestURI())
                                                .status(HttpStatus.NOT_FOUND.value())
                                                .build(),
                HttpStatus.NOT_FOUND);
    }

}
