package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.ResponseMessage.Constants;
import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
import com.example.ucakbiletotamasyonu.enums.FlightClass;
import com.example.ucakbiletotamasyonu.enums.SeatStatus;
import com.example.ucakbiletotamasyonu.mapper.SeatMapper;
import com.example.ucakbiletotamasyonu.model.Flight;
import com.example.ucakbiletotamasyonu.model.Seat;
import com.example.ucakbiletotamasyonu.repository.FlightRepository;
import com.example.ucakbiletotamasyonu.repository.SeatRepository;
import com.example.ucakbiletotamasyonu.service.ISeatService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class SeatServiceImpl implements ISeatService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatMapper seatMapper;

    @Override
    public GenericResponse<?> getSeatsByFlight(Integer flightId, SeatStatus status, FlightClass flightClass) {
        if (flightId == null) {
            return GenericResponse.error(Constants.EMPTY_FLIGHT);
        }

        Flight flight = flightRepository.findByIdAndDeletedFalse(flightId).orElse(null);
        if (flight == null) {
            return GenericResponse.error(Constants.EMPTY_FLIGHT);
        }

        List<Seat> seats;
        if (status != null && flightClass != null) {
            // Repository does not expose combined query; filter in-memory after narrowing by class.
            seats = seatRepository.findByFlightAndFlightClass(flight, flightClass).stream()
                    .filter(s -> s.getStatus() == status)
                    .toList();
        } else if (status != null) {
            seats = seatRepository.findByFlightAndStatus(flight, status);
        } else if (flightClass != null) {
            seats = seatRepository.findByFlightAndFlightClass(flight, flightClass);
        } else {
            seats = seatRepository.findByFlight(flight);
        }

        return GenericResponse.success(seats.stream().map(seatMapper::seatToDto).toList());
    }
}

