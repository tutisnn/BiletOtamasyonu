package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.dto.FlightDto;
import com.example.ucakbiletotamasyonu.mapper.FlightMapper;
import com.example.ucakbiletotamasyonu.model.Flight;
import com.example.ucakbiletotamasyonu.repository.FlightRepository;
import com.example.ucakbiletotamasyonu.service.IFlightService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FlightServiceImpl implements IFlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightMapper flightMapper;

    @Override
    public FlightDto saveFlight(FlightDto flightDto) {
        if (flightDto == null) {
            throw new RuntimeException("Flight data cannot be null");
        }

        Optional<Flight> existingFlight = flightRepository.findByFlightNo(flightDto.getFlightNo());
        if (existingFlight.isPresent()) {
            throw new RuntimeException("Flight already exists with flight number: " + flightDto.getFlightNo());
        }

        Flight flight = flightMapper.dtoToFlight(flightDto);
        Flight savedFlight = flightRepository.save(flight);
        return flightMapper.flightToDto(savedFlight);
    }

    @Override
    public List<FlightDto> getAllFlights() {
        List<Flight> flights = flightRepository.findAll();
        return flights.stream()
                .map(flightMapper::flightToDto)
                .toList();
    }

    @Override
    public FlightDto getFlightById(Integer id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found with id: " + id));

        return flightMapper.flightToDto(flight);
    }

    @Override
    public List<FlightDto> searchFlights(String departure, String arrival, LocalDate departureDate) {
        List<Flight> flights = flightRepository.findByDepartureAndArrival(departure, arrival);

        return flights.stream()
                .filter(flight -> flight.getDepartureTime() != null)
                .filter(flight -> flight.getDepartureTime().toLocalDate().equals(departureDate))
                .map(flightMapper::flightToDto)
                .toList();
    }

    @Override
    public void deleteFlightById(Integer id) {
        if (!flightRepository.existsById(id)) {
            throw new RuntimeException("Flight not found with id: " + id);
        }

        flightRepository.deleteById(id);
    }

    @Override
    public FlightDto updateFlight(Integer id, FlightDto updatedFlightDto) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found with id: " + id));

        flight.setFlightNo(updatedFlightDto.getFlightNo());
        flight.setDeparture(updatedFlightDto.getDeparture());
        flight.setArrival(updatedFlightDto.getArrival());
        flight.setDepartureTime(updatedFlightDto.getDepartureTime());
        flight.setArrivalTime(updatedFlightDto.getArrivalTime());
        flight.setPrice(updatedFlightDto.getPrice());
        flight.setCapacity(updatedFlightDto.getCapacity());
        flight.setAvailableSeats(updatedFlightDto.getAvailableSeats());
        flight.setStatus(updatedFlightDto.getStatus());

        Flight updatedFlight = flightRepository.save(flight);
        return flightMapper.flightToDto(updatedFlight);
    }
}
