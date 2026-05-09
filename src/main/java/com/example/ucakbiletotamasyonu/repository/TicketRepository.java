package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.model.Reservation;
import com.example.ucakbiletotamasyonu.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    Optional<Ticket> findByReservation(Reservation reservation);

    Optional<Ticket> findByTicketNumber(String ticketNumber);
}
