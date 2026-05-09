package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
import com.example.ucakbiletotamasyonu.dto.ReservationCreateDto;
import com.example.ucakbiletotamasyonu.dto.ReservationDto;

public interface IReservationService {

    GenericResponse<?> saveReservation(ReservationCreateDto reservationCreateDto);

    GenericResponse<?> getAllReservations();

    GenericResponse<?> getReservationById(Integer id);

    GenericResponse<?> getReservationsByUserId(Integer userId);

    GenericResponse<?> deleteReservationById(Integer id);

    GenericResponse<?> cancelReservation(Integer id);

    GenericResponse<?> updateReservation(Integer id, ReservationDto updatedReservationDto);
}
