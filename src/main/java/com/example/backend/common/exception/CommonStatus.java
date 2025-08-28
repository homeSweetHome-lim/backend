package com.example.backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonStatus implements Status{
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "", 400)

    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final int statusCode;


}
