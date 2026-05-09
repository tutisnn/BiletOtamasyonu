package com.example.ucakbiletotamasyonu.ResponseMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor

public class GenericResponse<T> {
    private T data;
    private String message;

    public static <T> GenericResponse<T> error(String message) { //findBy, delete, update için
        return GenericResponse.<T>builder()
                .message(message)
                .build();
    }

    public static <T> GenericResponse<T> success(T data) {
        return GenericResponse.<T>builder()
                .message("İşlem başarılı")
                .data(data)
                .build();
    }

}
