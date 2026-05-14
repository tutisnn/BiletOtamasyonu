package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
import com.example.ucakbiletotamasyonu.enums.FlightClass;
import com.example.ucakbiletotamasyonu.enums.SeatStatus;

public interface ISeatService {

    GenericResponse<?> getSeatsByFlight(Integer flightId, SeatStatus status, FlightClass flightClass);
}

