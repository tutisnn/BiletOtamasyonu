package com.example.ucakbiletotamasyonu.mapper;

import com.example.ucakbiletotamasyonu.dto.SeatDto;
import com.example.ucakbiletotamasyonu.model.Flight;
import com.example.ucakbiletotamasyonu.model.Seat;
import org.springframework.stereotype.Component;

@Component
public class SeatMapper {

    public Seat dtoToSeat(SeatDto dto, Flight flight) {
        if (dto == null || flight == null) return null;

        Seat seat = new Seat();
        seat.setSeatNumber(dto.getSeatNumber());
        seat.setStatus(dto.getStatus());
        seat.setFlight(flight);
        return seat;
    }

    public SeatDto seatToDto(Seat seat) {
        if (seat == null) return null;

        SeatDto dto = new SeatDto();
        dto.setId(seat.getId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setStatus(seat.getStatus());

        if (seat.getFlight() != null) {
            dto.setFlightId(seat.getFlight().getId());
            dto.setFlightNum(seat.getFlight().getFlightNo());
        }

        return dto;
    }
}
