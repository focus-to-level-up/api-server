package com.studioedge.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExceptionResponse(int status, String message) {

    public static ExceptionResponse of(int status, String message) {
        return new ExceptionResponse(status, message);
    }

    public static ExceptionResponse of(HttpStatus status, String message) {
        return new ExceptionResponse(status.value(), message);
    }
}
