package com.example.ucakbiletotamasyonu.dto;

import com.example.ucakbiletotamasyonu.enums.BaggageOption;
import com.example.ucakbiletotamasyonu.enums.EntertainmentOption;
import com.example.ucakbiletotamasyonu.enums.FlightClass;
import com.example.ucakbiletotamasyonu.enums.PassengerType;
import com.example.ucakbiletotamasyonu.enums.ReservationStatus;
import com.example.ucakbiletotamasyonu.enums.WifiOption;
import java.math.BigDecimal;
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
    private FlightClass flightClass;
    private PassengerType passengerType;
    private BaggageOption baggageOption;
    private WifiOption wifiOption;
    private EntertainmentOption entertainmentOption;
    private BigDecimal totalPrice;
    private String flightNum;
    private String seatNumber;
    private String passengerFullName;
    private String userEmail;
}
