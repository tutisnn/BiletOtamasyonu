package com.example.ucakbiletotamasyonu.mapper;

import com.example.ucakbiletotamasyonu.dto.TicketDto;
import com.example.ucakbiletotamasyonu.model.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketDto ticketToDto(Ticket ticket) {
        if (ticket == null) return null;

        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setCreateTime(ticket.getCreateTime());
        dto.setTicketNumber(ticket.getTicketNumber());

        if (ticket.getReservation() != null) {
            dto.setReservationId(ticket.getReservation().getId());

            if (ticket.getReservation().getFlight() != null) {
                dto.setFlightNum(ticket.getReservation().getFlight().getFlightNo());
            }

            if (ticket.getReservation().getPassenger() != null) {
                dto.setPassengerFullName(
                        ticket.getReservation().getPassenger().getFirstName() + " " +
                                ticket.getReservation().getPassenger().getLastName()
                );
            }

            if (ticket.getReservation().getSeat() != null) {
                dto.setSeatNumber(ticket.getReservation().getSeat().getSeatNumber());
            }
        }

        return dto;
    }
}
