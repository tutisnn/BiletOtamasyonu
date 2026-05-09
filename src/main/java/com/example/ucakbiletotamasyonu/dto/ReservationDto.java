package com.example.ucakbiletotamasyonu.dto;

import com.example.ucakbiletotamasyonu.enums.ReservationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationDto extends DtoBase {

    private Integer flightId;
    private Integer seatId;
    private Integer passengerId;
    private Integer userId;
    private ReservationStatus status;
    private LocalDateTime reservationDate;
    private String flightNum;
    private String seatNumber;
    private String passengerFullName;
    private String userEmail;
}
