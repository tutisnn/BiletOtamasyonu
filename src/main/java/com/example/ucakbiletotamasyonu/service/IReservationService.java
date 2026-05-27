package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.dto.ReservationCreateDto;
import com.example.ucakbiletotamasyonu.dto.ReservationDto;
import java.util.List;

public interface IReservationService {

    ReservationDto saveReservation(ReservationCreateDto reservationCreateDto);

    List<ReservationDto> getAllReservations();

    ReservationDto getReservationById(Integer id);

    List<ReservationDto> getReservationsByUserId(Integer userId);

    String deleteReservationById(Integer id);

    ReservationDto cancelReservation(Integer id);

    ReservationDto updateReservation(Integer id, ReservationDto updatedReservationDto);
}
