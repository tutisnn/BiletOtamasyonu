package com.example.ucakbiletotamasyonu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.ucakbiletotamasyonu.enums.BaggageOption;
import com.example.ucakbiletotamasyonu.enums.EntertainmentOption;
import com.example.ucakbiletotamasyonu.enums.FlightClass;
import com.example.ucakbiletotamasyonu.enums.PassengerType;
import com.example.ucakbiletotamasyonu.enums.WifiOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // Backward-compat if older clients still send removed fields like userId
public class ReservationCreateDto {

    @NotNull
    private Integer flightId;
    @NotNull
    private Integer seatId;

    @NotNull
    private FlightClass flightClass;
    private PassengerType passengerType;
    private BaggageOption baggageOption;
    private WifiOption wifiOption;
    private EntertainmentOption entertainmentOption;

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String identityNumber;
    private String passportNumber;
    private String email;
    private String phoneNumber;
}
