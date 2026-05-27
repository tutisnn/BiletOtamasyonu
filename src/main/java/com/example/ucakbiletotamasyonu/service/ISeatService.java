package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.dto.SeatDto;
import com.example.ucakbiletotamasyonu.enums.FlightClass;
import com.example.ucakbiletotamasyonu.enums.SeatStatus;
import java.util.List;

public interface ISeatService {

    List<SeatDto> getSeatsByFlight(Integer flightId, SeatStatus status, FlightClass flightClass);
}
