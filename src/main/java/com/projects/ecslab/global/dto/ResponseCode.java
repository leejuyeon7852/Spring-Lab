package com.projects.ecslab.global.dto;

import org.springframework.http.HttpStatus;

public enum ResponseCode {

    MEMBER_CREATED(
            HttpStatus.CREATED,
            "회원가입이 완료되었습니다."
    ),
    DUPLICATE_USERNAME(
            HttpStatus.CONFLICT,
            "이미 사용 중인 아이디입니다."
    ),
    LOGIN_SUCCESS(
            HttpStatus.OK,
            "로그인에 성공했습니다."
    ),
    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "아이디 또는 비밀번호가 올바르지 않습니다."
    ),
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "잘못된 요청입니다."),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ResponseCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String message() {
        return message;
    }
}