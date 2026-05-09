package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.dto.FlightDto;

import java.time.LocalDate;
import java.util.List;

public interface IFlightService {

    FlightDto saveFlight(FlightDto flightDto);

    List<FlightDto> getAllFlights();

    FlightDto getFlightById(Integer id);

    List<FlightDto> searchFlights(String departure, String arrival, LocalDate departureDate);

    void deleteFlightById(Integer id);

    FlightDto updateFlight(Integer id, FlightDto updatedFlightDto);
}
