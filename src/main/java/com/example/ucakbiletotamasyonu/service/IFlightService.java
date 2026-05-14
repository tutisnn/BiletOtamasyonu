package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.dto.FlightDto;

import java.time.LocalDate;
import java.util.List;
import com.example.ucakbiletotamasyonu.dto.AirportOptionDto;

public interface IFlightService {

    FlightDto saveFlight(FlightDto flightDto);

    List<FlightDto> getAllFlights();

    FlightDto getFlightById(Integer id);

    List<FlightDto> searchFlights(String departureCity, String arrivalCity, LocalDate departureDate,
                                  String departureAirport, String arrivalAirport);

    void deleteFlightById(Integer id);

    FlightDto updateFlight(Integer id, FlightDto updatedFlightDto);

    List<AirportOptionDto> getAirportOptions();
}
