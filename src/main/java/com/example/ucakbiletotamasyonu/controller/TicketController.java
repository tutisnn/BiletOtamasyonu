package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.service.ITicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController extends RestBaseController {

    @Autowired
    private ITicketService ticketService;

    @GetMapping("/getAll")
    public RootEntity<?> getAllTickets() {
        return ok(ticketService.getAllTickets());
    }

    @GetMapping("/getTicket/{id}")
    public RootEntity<?> getTicketById(@PathVariable Integer id) {
        return ok(ticketService.getTicketById(id));
    }

    @GetMapping("/getByReservation/{reservationId}")
    public RootEntity<?> getTicketByReservationId(@PathVariable Integer reservationId) {
        return ok(ticketService.getTicketByReservationId(reservationId));
    }

    @GetMapping("/getByUser/{userId}")
    public RootEntity<?> getTicketsByUserId(@PathVariable Integer userId) {
        return ok(ticketService.getTicketsByUserId(userId));
    }

    // Logged-in user's tickets (user resolved from JWT / SecurityContext)
    @GetMapping("/my")
    public RootEntity<?> getMyTickets() {
        return ok(ticketService.getMyTickets());
    }

    @DeleteMapping("/delete/{id}")
    public RootEntity<?> deleteTicketById(@PathVariable Integer id) {
        return ok(ticketService.deleteTicketById(id));
    }
}
