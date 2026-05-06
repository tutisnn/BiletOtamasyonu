package com.example.ucakbiletotamasyonu.exception;

import lombok.Getter;

@Getter
public enum MessageType {
    NO_RECORD_EXIST("1004", "record not found"),
    TOKEN_IS_EXPIRED("1005", "token is expired"),
    EMAIL_NOT_FOUND("1006", "email not found"),
    EMAIL_OR_PASSWORD_INVALID("1007", "email or password is invalid"),
    EMAIL_ALREADY_REGISTERED("1008", "email already registered"),
    REFRESH_TOKEN_NOT_FOUND("1009", "refresh token not found"),
    REFRESH_TOKEN_IS_EXPIRED("1010", "refresh token is expired"),
    EMAIL_NOT_VERIFIED("1011", "email is not verified"),
    VERIFICATION_CODE_INVALID("1012", "verification code is invalid"),
    VERIFICATION_CODE_EXPIRED("1013", "verification code is expired"),
    VERIFICATION_EMAIL_SEND_FAILED("1014", "verification email send failed"),
    EMAIL_ALREADY_VERIFIED("1015", "email is already verified"),
    GENERAL_EXCEPTION("9999", "general exception occurred");

    private final String code;
    private final String message;

    MessageType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
