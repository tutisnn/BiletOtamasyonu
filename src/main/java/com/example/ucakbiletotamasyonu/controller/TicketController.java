package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
import com.example.ucakbiletotamasyonu.service.ITicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private ITicketService ticketService;

    @GetMapping("/getAll")
    public GenericResponse<?> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/getTicket/{id}")
    public GenericResponse<?> getTicketById(@PathVariable Integer id) {
        return ticketService.getTicketById(id);
    }

    @GetMapping("/getByReservation/{reservationId}")
    public GenericResponse<?> getTicketByReservationId(@PathVariable Integer reservationId) {
        return ticketService.getTicketByReservationId(reservationId);
    }

    @GetMapping("/getByUser/{userId}")
    public GenericResponse<?> getTicketsByUserId(@PathVariable Integer userId) {
        return ticketService.getTicketsByUserId(userId);
    }

    // Logged-in user's tickets (user resolved from JWT / SecurityContext)
    @GetMapping("/my")
    public GenericResponse<?> getMyTickets() {
        return ticketService.getMyTickets();
    }

    @DeleteMapping("/delete/{id}")
    public GenericResponse<?> deleteTicketById(@PathVariable Integer id) {
        return ticketService.deleteTicketById(id);
    }
}
