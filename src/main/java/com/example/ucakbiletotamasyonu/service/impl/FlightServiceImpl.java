package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.dto.FlightDto;
import com.example.ucakbiletotamasyonu.enums.FlightClass;
import com.example.ucakbiletotamasyonu.enums.SeatStatus;
import com.example.ucakbiletotamasyonu.mapper.FlightMapper;
import com.example.ucakbiletotamasyonu.model.Flight;
import com.example.ucakbiletotamasyonu.model.Seat;
import com.example.ucakbiletotamasyonu.repository.FlightRepository;
import com.example.ucakbiletotamasyonu.repository.SeatRepository;
import com.example.ucakbiletotamasyonu.service.IFlightService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FlightServiceImpl implements IFlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SeatRepository seatRepository;

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
        int totalSeatCount = 144;
        flight.setCapacity(totalSeatCount);
        flight.setAvailableSeats(totalSeatCount);

        Flight savedFlight = flightRepository.save(flight);
        List<Seat> seats = generateSeatsForFlight(savedFlight);
        seatRepository.saveAll(seats);
        savedFlight.setSeats(seats);

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
        flight.setAirline(updatedFlightDto.getAirline());
        flight.setDeparture(updatedFlightDto.getDeparture());
        flight.setArrival(updatedFlightDto.getArrival());
        flight.setDepartureTime(updatedFlightDto.getDepartureTime());
        flight.setArrivalTime(updatedFlightDto.getArrivalTime());
        flight.setPrice(updatedFlightDto.getPrice());
        flight.setStatus(updatedFlightDto.getStatus());

        Flight updatedFlight = flightRepository.save(flight);
        return flightMapper.flightToDto(updatedFlight);
    }

    private List<Seat> generateSeatsForFlight(Flight flight) {
        List<Seat> seats = new ArrayList<>();

        seats.addAll(generateFirstClassSeats(flight));
        seats.addAll(generateBusinessClassSeats(flight));
        seats.addAll(generateEconomyClassSeats(flight));

        return seats;
    }

    private List<Seat> generateFirstClassSeats(Flight flight) {
        List<Seat> seats = new ArrayList<>();

        int[] rows = {1, 2};
        char[] cols = {'A', 'B'};

        for (int row : rows) {
            for (char col : cols) {
                seats.add(createSeat(flight, row + String.valueOf(col), FlightClass.FIRST_CLASS));
            }
        }

        return seats;
    }

    private List<Seat> generateBusinessClassSeats(Flight flight) {
        List<Seat> seats = new ArrayList<>();

        for (int row = 3; row <= 7; row++) {
            for (char col : new char[]{'A', 'B', 'C', 'D'}) {
                seats.add(createSeat(flight, row + String.valueOf(col), FlightClass.BUSINESS));
            }
        }

        return seats;
    }

    private List<Seat> generateEconomyClassSeats(Flight flight) {
        List<Seat> seats = new ArrayList<>();

        for (int row = 8; row <= 27; row++) {
            for (char col : new char[]{'A', 'B', 'C', 'D', 'E', 'F'}) {
                seats.add(createSeat(flight, row + String.valueOf(col), FlightClass.ECONOMY));
            }
        }

        return seats;
    }

    private Seat createSeat(Flight flight, String seatNumber, FlightClass flightClass) {
        Seat seat = new Seat();
        seat.setFlight(flight);
        seat.setSeatNumber(seatNumber);
        seat.setFlightClass(flightClass);
        seat.setStatus(SeatStatus.AVAILABLE);
        return seat;
    }
}
