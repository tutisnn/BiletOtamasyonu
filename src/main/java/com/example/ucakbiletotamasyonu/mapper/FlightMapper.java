package com.example.ucakbiletotamasyonu.mapper;

import com.example.ucakbiletotamasyonu.dto.FlightDto;
import com.example.ucakbiletotamasyonu.dto.AirportInfoDto;
import com.example.ucakbiletotamasyonu.model.AirportInfo;
import com.example.ucakbiletotamasyonu.model.Flight;
import org.springframework.stereotype.Component;

@Component
public class FlightMapper {

    public Flight dtoToFlight(FlightDto dto) {
        if (dto == null) return null;

        Flight flight = new Flight();
        flight.setFlightNo(dto.getFlightNo());
        flight.setAirline(dto.getAirline());
        flight.setDeparture(toAirportInfo(dto.getDeparture()));
        flight.setArrival(toAirportInfo(dto.getArrival()));
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setPrice(dto.getPrice());
        flight.setStatus(dto.getStatus());
        return flight;
    }

    public FlightDto flightToDto(Flight flight) {
        if (flight == null) return null;

        FlightDto dto = new FlightDto();
        dto.setId(flight.getId());
        dto.setFlightNo(flight.getFlightNo());
        dto.setAirline(flight.getAirline());
        dto.setDeparture(toAirportInfoDto(flight.getDeparture()));
        dto.setArrival(toAirportInfoDto(flight.getArrival()));
        dto.setDepartureTime(flight.getDepartureTime());
        dto.setArrivalTime(flight.getArrivalTime());
        dto.setPrice(flight.getPrice());
        dto.setAvailableSeats(flight.getAvailableSeats());
        dto.setStatus(flight.getStatus());
        return dto;
    }

    private AirportInfo toAirportInfo(AirportInfoDto dto) {
        if (dto == null) {
            return null;
        }
        AirportInfo info = new AirportInfo();
        info.setCity(dto.getCity());
        info.setAirport(dto.getAirport());
        return info;
    }

    private AirportInfoDto toAirportInfoDto(AirportInfo info) {
        if (info == null) {
            return null;
        }
        return new AirportInfoDto(info.getCity(), info.getAirport());
    }
}
