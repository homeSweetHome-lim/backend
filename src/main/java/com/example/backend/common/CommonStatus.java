package com.example.backend.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonStatus implements Status {
    LAWD_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 지역 코드가 존재하지 않습니다.", 404),
    NO_RESPONSE(HttpStatus.BAD_REQUEST, "응답이 없습니다", 400),


    //유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저가 존재하지 않습니다.", 404),
    ALREADY_SIGNUP_USER(HttpStatus.BAD_REQUEST, "이미 존재하는 유저입니다.", 400),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다", 400),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", 400),

    // 매물
    PROPERTY_NOT_FOUND(HttpStatus.NOT_FOUND, "매물이 존재하지 않습니다.", 404),

    // 좋아요 목록
    ALREADY_ADD_FAVORITE(, , );

    private final HttpStatus httpStatus;
    private final String message;
    private final int statusCode;

}
