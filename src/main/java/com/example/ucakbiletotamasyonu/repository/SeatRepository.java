package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.enums.SeatStatus;
import com.example.ucakbiletotamasyonu.model.Flight;
import com.example.ucakbiletotamasyonu.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {

    List<Seat> findByFlight(Flight flight);

    List<Seat> findByFlightAndStatus(Flight flight, SeatStatus status);

    Optional<Seat> findByFlightAndSeatNumber(Flight flight, String seatNumber);
}
