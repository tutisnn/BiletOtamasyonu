package com.example.ucakbiletotamasyonu.dto;

import com.example.ucakbiletotamasyonu.enums.BaggageOption;
import com.example.ucakbiletotamasyonu.enums.EntertainmentOption;
import com.example.ucakbiletotamasyonu.enums.FlightClass;
import com.example.ucakbiletotamasyonu.enums.PassengerType;
import com.example.ucakbiletotamasyonu.enums.WifiOption;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationCreateDto {

    private Integer flightId;
    private Integer seatId;
    private Integer userId;

    private FlightClass flightClass;
    private PassengerType passengerType;
    private BaggageOption baggageOption;
    private WifiOption wifiOption;
    private EntertainmentOption entertainmentOption;

    private String firstName;
    private String lastName;
    private String identityNumber;
    private String passportNumber;
    private String email;
    private String phoneNumber;
}
