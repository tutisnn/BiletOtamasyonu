package com.example.ucakbiletotamasyonu.dto;

import com.example.ucakbiletotamasyonu.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentDto extends DtoBase {

    private Integer reservationId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String flightNum;
    private String passengerFullName;
}
