package com.itzap.elasticsearch.exceptions;

import com.itzap.common.ErrorCode;

public enum EsErrors implements ErrorCode {
    INVALID_SETUP("itzap.es.invalid_setup", "Elastic search configuration error");

    private final String errorCode;
    private final String message;

    EsErrors(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
