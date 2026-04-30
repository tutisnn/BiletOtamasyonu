package com.example.ucakbiletotamasyonu.exception;

import lombok.Getter;

@Getter
public enum MessageType {
    NO_RECORD_EXIST("1004", "record not found"),
    TOKEN_IS_EXPIRED("1005", "token is expired"),
    EMAIL_NOT_FOUND("1006", "email not found"),
    EMAIL_OR_PASSWORD_INVALID("1007", "email or password is invalid"),
    REFRESH_TOKEN_NOT_FOUND("1008", "refresh token not found"),
    REFRESH_TOKEN_IS_EXPIRED("1009", "refresh token is expired"),
    GENERAL_EXCEPTION("9999", "general exception occurred");

    private final String code;
    private final String message;

    MessageType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
