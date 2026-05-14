package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Integer> {

    Optional<Flight> findByFlightNoAndDeletedFalse(String flightNo);

    List<Flight> findByDeparture_CityAndArrival_CityAndDeletedFalse(String departureCity, String arrivalCity);

    List<Flight> findByDeparture_CityAndDeparture_AirportAndArrival_CityAndArrival_AirportAndDeletedFalse(
            String departureCity,
            String departureAirport,
            String arrivalCity,
            String arrivalAirport
    );

    List<Flight> findByStatusAndDeletedFalse(com.example.ucakbiletotamasyonu.enums.FlightStatus status);

    List<Flight> findByDeletedFalse();

    Optional<Flight> findByIdAndDeletedFalse(Integer id);

    boolean existsByIdAndDeletedFalse(Integer id);

    @Query("""
            select distinct f.departure.city, f.departure.airport
            from Flight f
            where f.deleted = false
              and f.departure.city is not null
              and f.departure.airport is not null
            """)
    List<Object[]> findDistinctDepartureAirportOptions();

    @Query("""
            select distinct f.arrival.city, f.arrival.airport
            from Flight f
            where f.deleted = false
              and f.arrival.city is not null
              and f.arrival.airport is not null
            """)
    List<Object[]> findDistinctArrivalAirportOptions();
}
