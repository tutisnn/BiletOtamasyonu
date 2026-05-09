package com.example.ucakbiletotamasyonu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutResponseDto {

    private Integer reservationId;
    private Integer paymentId;
    private String sessionId;
    private String sessionUrl;
    private String status;
}
