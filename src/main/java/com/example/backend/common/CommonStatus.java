package com.example.backend.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonStatus implements Status {
    LAWD_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 지역 코드가 존재하지 않습니다.", 404),
    NO_RESPONSE(HttpStatus.BAD_REQUEST, "응답이 없습니다", 400
        );

    private final HttpStatus httpStatus;
    private final String message;
    private final int statusCode;

}
