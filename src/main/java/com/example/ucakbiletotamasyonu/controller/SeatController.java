package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
import com.example.ucakbiletotamasyonu.enums.FlightClass;
import com.example.ucakbiletotamasyonu.enums.SeatStatus;
import com.example.ucakbiletotamasyonu.service.ISeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private ISeatService seatService;

    @GetMapping("/by-flight/{flightId}")
    public GenericResponse<?> getSeatsByFlight(
            @PathVariable Integer flightId,
            @RequestParam(required = false, defaultValue = "AVAILABLE") SeatStatus status,
            @RequestParam(required = false) FlightClass flightClass
    ) {
        return seatService.getSeatsByFlight(flightId, status, flightClass);
    }
}

