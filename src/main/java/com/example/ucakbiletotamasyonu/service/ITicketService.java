package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;

public interface ITicketService {

    GenericResponse<?> createTicketFromReservation(Integer reservationId);

    GenericResponse<?> getAllTickets();

    GenericResponse<?> getTicketById(Integer id);

    GenericResponse<?> getTicketByReservationId(Integer reservationId);

    GenericResponse<?> getTicketsByUserId(Integer userId);

    GenericResponse<?> getMyTickets();

    GenericResponse<?> deleteTicketById(Integer id);
}
