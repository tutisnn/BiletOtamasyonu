package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
import com.example.ucakbiletotamasyonu.dto.FlightDto;
import com.example.ucakbiletotamasyonu.service.IFlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    @Autowired
    private IFlightService flightService;

    @PostMapping("/save")
    public GenericResponse<?> saveFlight(@RequestBody FlightDto flightDto) {
        return GenericResponse.success(flightService.saveFlight(flightDto));
    }

    @GetMapping("/getAll")
    public GenericResponse<?> getAllFlights() {
        return GenericResponse.success(flightService.getAllFlights());
    }

    @GetMapping("/getFlight/{id}")
    public GenericResponse<?> getFlightById(@PathVariable Integer id) {
        return GenericResponse.success(flightService.getFlightById(id));
    }

    @GetMapping("/search")
    public GenericResponse<?> searchFlights(
            @RequestParam String departure,
            @RequestParam String arrival,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate
    ) {
        return GenericResponse.success(
                flightService.searchFlights(departure, arrival, departureDate)
        );
    }

    @DeleteMapping("/delete/{id}")
    public GenericResponse<?> deleteFlightById(@PathVariable Integer id) {
        flightService.deleteFlightById(id);
        return GenericResponse.success("Flight deleted successfully");
    }

    @PutMapping("/update/{id}")
    public GenericResponse<?> updateFlight(@PathVariable Integer id, @RequestBody FlightDto flightDto) {
        return GenericResponse.success(flightService.updateFlight(id, flightDto));
    }
}
