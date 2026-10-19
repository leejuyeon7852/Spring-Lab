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
    TOKEN_MEMBER_NOT_FOUND(
            HttpStatus.UNAUTHORIZED,
            "토큰에 해당하는 회원이 존재하지 않습니다."
    ),
    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "유효하지 않은 토큰입니다."
    ),
    EXPIRED_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "만료된 토큰입니다."
    ),
    TOKEN_REFRESHED(
            HttpStatus.OK,
            "토큰이 재발급되었습니다."
    ),
    AUTHENTICATION_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "인증이 필요합니다."
    ),
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "접근 권한이 없습니다."
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