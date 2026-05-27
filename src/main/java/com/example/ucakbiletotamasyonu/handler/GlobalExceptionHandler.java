package com.example.ucakbiletotamasyonu.handler;

import com.example.ucakbiletotamasyonu.controller.RootEntity;
import com.example.ucakbiletotamasyonu.exception.BaseException;
import com.example.ucakbiletotamasyonu.exception.MessageType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BaseException.class)
    public ResponseEntity<RootEntity<?>> handleBaseException(BaseException ex, WebRequest request) {
        HttpStatus status = switch (ex.getErrorMessage().getMessageType()) {
            case VOICE_AUDIO_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case NO_RECORD_EXIST, EMAIL_NOT_FOUND, REFRESH_TOKEN_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TOKEN_IS_EXPIRED, REFRESH_TOKEN_IS_EXPIRED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(RootEntity.error(status.value(), ex.getMessage()));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<RootEntity<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, List<String>> errors = new HashMap<>();

        for (ObjectError objectError : ex.getBindingResult().getAllErrors()) {
            String fieldName = ((FieldError) objectError).getField();

            if (errors.containsKey(fieldName)) {
                errors.put(fieldName, addValue(errors.get(fieldName), objectError.getDefaultMessage()));
            } else {
                errors.put(fieldName, addValue(new ArrayList<>(), objectError.getDefaultMessage()));
            }
        }

        // Keep RootEntity schema; include field errors in the message for now.
        return ResponseEntity.badRequest().body(RootEntity.error(HttpStatus.BAD_REQUEST.value(), errors.toString()));
    }

    private List<String> addValue(List<String> list, String newValue) {
        list.add(newValue);
        return list;
    }
}
