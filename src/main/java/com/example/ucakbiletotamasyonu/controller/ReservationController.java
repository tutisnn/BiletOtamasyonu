package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.dto.ReservationCreateDto;
import com.example.ucakbiletotamasyonu.dto.ReservationDto;
import com.example.ucakbiletotamasyonu.service.IReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController extends RestBaseController {

    @Autowired
    private IReservationService reservationService;

    @PostMapping("/save")
    public RootEntity<ReservationDto> saveReservation(@Valid @RequestBody ReservationCreateDto reservationCreateDto) {
        return ok(reservationService.saveReservation(reservationCreateDto));
    }

    @GetMapping("/getAll")
    public RootEntity<?> getAllReservations() {
        return ok(reservationService.getAllReservations());
    }

    @GetMapping("/getReservation/{id}")
    public RootEntity<ReservationDto> getReservationById(@PathVariable Integer id) {
        return ok(reservationService.getReservationById(id));
    }

    @GetMapping("/getByUser/{userId}")
    public RootEntity<?> getReservationsByUserId(@PathVariable Integer userId) {
        return ok(reservationService.getReservationsByUserId(userId));
    }

    @DeleteMapping("/delete/{id}")
    public RootEntity<?> deleteReservationById(@PathVariable Integer id) {
        return ok(reservationService.deleteReservationById(id));
    }

    @PutMapping("/cancel/{id}")
    public RootEntity<ReservationDto> cancelReservation(@PathVariable Integer id) {
        return ok(reservationService.cancelReservation(id));
    }

    @PutMapping("/update/{id}")
    public RootEntity<ReservationDto> updateReservation(@PathVariable Integer id,
                                                        @RequestBody ReservationDto reservationDto) {
        return ok(reservationService.updateReservation(id, reservationDto));
    }
}
