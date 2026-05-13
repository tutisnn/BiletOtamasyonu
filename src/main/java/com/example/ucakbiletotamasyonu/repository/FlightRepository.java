package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Integer> {

    Optional<Flight> findByFlightNoAndDeletedFalse(String flightNo);

    List<Flight> findByDepartureAndArrivalAndDeletedFalse(String departure, String arrival);

    List<Flight> findByStatusAndDeletedFalse(com.example.ucakbiletotamasyonu.enums.FlightStatus status);

    List<Flight> findByDeletedFalse();

    Optional<Flight> findByIdAndDeletedFalse(Integer id);

    boolean existsByIdAndDeletedFalse(Integer id);
}
