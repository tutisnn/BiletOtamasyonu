package com.example.ucakbiletotamasyonu.dto;

import com.example.ucakbiletotamasyonu.enums.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlightDto {
    private Integer id;
    private String flightNo;
    private String airline;
    private String departure;
    private String arrival;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal price;
    private Integer capacity;
    private Integer availableSeats;
    private FlightStatus status;
}
