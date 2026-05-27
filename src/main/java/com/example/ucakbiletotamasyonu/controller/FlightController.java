package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.dto.AirportOptionDto;
import com.example.ucakbiletotamasyonu.dto.FlightDto;
import com.example.ucakbiletotamasyonu.service.IFlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/flights")
public class FlightController extends RestBaseController {

    @Autowired
    private IFlightService flightService;

    @PostMapping("/save")
    public RootEntity<FlightDto> saveFlight(@RequestBody FlightDto flightDto) {
        return ok(flightService.saveFlight(flightDto));
    }

    @GetMapping("/getAll")
    public RootEntity<?> getAllFlights() {
        return ok(flightService.getAllFlights());
    }

    @GetMapping("/getFlight/{id}")
    public RootEntity<FlightDto> getFlightById(@PathVariable Integer id) {
        return ok(flightService.getFlightById(id));
    }

    @GetMapping("/search")
    public RootEntity<?> searchFlights(
            @RequestParam String departure,
            @RequestParam String arrival,
            @RequestParam(required = false) String departureAirport,
            @RequestParam(required = false) String arrivalAirport,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate
    ) {
        return ok(flightService.searchFlights(departure, arrival, departureDate, departureAirport, arrivalAirport));
    }

    @GetMapping("/airports")
    public RootEntity<?> getAirportOptions() {
        return ok(flightService.getAirportOptions());
    }

    @DeleteMapping("/delete/{id}")
    public RootEntity<?> deleteFlightById(@PathVariable Integer id) {
        flightService.deleteFlightById(id);
        return ok("Flight deleted successfully");
    }

    @PutMapping("/update/{id}")
    public RootEntity<FlightDto> updateFlight(@PathVariable Integer id, @RequestBody FlightDto flightDto) {
        return ok(flightService.updateFlight(id, flightDto));
    }
}
