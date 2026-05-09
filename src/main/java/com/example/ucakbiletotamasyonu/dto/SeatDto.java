package com.example.ucakbiletotamasyonu.dto;

import com.example.ucakbiletotamasyonu.enums.FlightClass;
import com.example.ucakbiletotamasyonu.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {

    private Integer id;
    private Integer flightId;
    private String flightNum;
    private String seatNumber;
    private SeatStatus status;
    private FlightClass flightClass;
}
