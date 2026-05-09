package com.example.ucakbiletotamasyonu.mapper;

import com.example.ucakbiletotamasyonu.dto.FlightDto;
import com.example.ucakbiletotamasyonu.model.Flight;
import org.springframework.stereotype.Component;

@Component
public class FlightMapper {

    public Flight dtoToFlight(FlightDto dto) {
        if (dto == null) return null;

        Flight flight = new Flight();
        flight.setFlightNo(dto.getFlightNo());
        flight.setAirline(dto.getAirline());
        flight.setDeparture(dto.getDeparture());
        flight.setArrival(dto.getArrival());
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setPrice(dto.getPrice());
        flight.setCapacity(dto.getCapacity());
        flight.setAvailableSeats(dto.getAvailableSeats());
        flight.setStatus(dto.getStatus());
        return flight;
    }

    public FlightDto flightToDto(Flight flight) {
        if (flight == null) return null;

        FlightDto dto = new FlightDto();
        dto.setId(flight.getId());
        dto.setFlightNo(flight.getFlightNo());
        dto.setAirline(flight.getAirline());
        dto.setDeparture(flight.getDeparture());
        dto.setArrival(flight.getArrival());
        dto.setDepartureTime(flight.getDepartureTime());
        dto.setArrivalTime(flight.getArrivalTime());
        dto.setPrice(flight.getPrice());
        dto.setCapacity(flight.getCapacity());
        dto.setAvailableSeats(flight.getAvailableSeats());
        dto.setStatus(flight.getStatus());
        return dto;
    }
}
