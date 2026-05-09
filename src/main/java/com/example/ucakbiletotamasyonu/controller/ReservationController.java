package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
import com.example.ucakbiletotamasyonu.dto.ReservationCreateDto;
import com.example.ucakbiletotamasyonu.dto.ReservationDto;
import com.example.ucakbiletotamasyonu.service.IReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private IReservationService reservationService;

    @PostMapping("/save")
    public GenericResponse<?> saveReservation(@RequestBody ReservationCreateDto reservationCreateDto) {
        return reservationService.saveReservation(reservationCreateDto);
    }

    @GetMapping("/getAll")
    public GenericResponse<?> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/getReservation/{id}")
    public GenericResponse<?> getReservationById(@PathVariable Integer id) {
        return reservationService.getReservationById(id);
    }

    @GetMapping("/getByUser/{userId}")
    public GenericResponse<?> getReservationsByUserId(@PathVariable Integer userId) {
        return reservationService.getReservationsByUserId(userId);
    }

    @DeleteMapping("/delete/{id}")
    public GenericResponse<?> deleteReservationById(@PathVariable Integer id) {
        return reservationService.deleteReservationById(id);
    }

    @PutMapping("/cancel/{id}")
    public GenericResponse<?> cancelReservation(@PathVariable Integer id) {
        return reservationService.cancelReservation(id);
    }

    @PutMapping("/update/{id}")
    public GenericResponse<?> updateReservation(@PathVariable Integer id,
                                                @RequestBody ReservationDto reservationDto) {
        return reservationService.updateReservation(id, reservationDto);
    }
}
