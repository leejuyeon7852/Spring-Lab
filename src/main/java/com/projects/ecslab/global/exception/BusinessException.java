package com.projects.ecslab.global.exception;

import com.projects.ecslab.global.dto.ResponseCode;

public class BusinessException extends RuntimeException {

    private final ResponseCode responseCode;

    public BusinessException(ResponseCode responseCode) {
        super(responseCode.message());
        this.responseCode = responseCode;
    }

    public ResponseCode responseCode() {
        return responseCode;
    }
}