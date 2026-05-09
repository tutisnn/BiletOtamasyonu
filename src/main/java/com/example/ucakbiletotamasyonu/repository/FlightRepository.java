package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Integer> {

    Optional<Flight> findByFlightNo(String flightNo);

    List<Flight> findByDepartureAndArrival(String departure, String arrival);

    List<Flight> findByStatus(com.example.ucakbiletotamasyonu.enums.FlightStatus status);
}
