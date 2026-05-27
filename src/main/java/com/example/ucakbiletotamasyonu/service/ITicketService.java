package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.dto.TicketDto;
import java.util.List;

public interface ITicketService {

    TicketDto createTicketFromReservation(Integer reservationId);

    List<TicketDto> getAllTickets();

    TicketDto getTicketById(Integer id);

    TicketDto getTicketByReservationId(Integer reservationId);

    List<TicketDto> getTicketsByUserId(Integer userId);

    List<TicketDto> getMyTickets();

    String deleteTicketById(Integer id);
}
