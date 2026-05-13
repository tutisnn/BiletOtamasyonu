package com.example.ucakbiletotamasyonu.mapper;

import com.example.ucakbiletotamasyonu.dto.ReservationDto;
import com.example.ucakbiletotamasyonu.model.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationDto reservationToDto(Reservation reservation) {
        if (reservation == null) return null;

        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getId());
        dto.setCreateTime(reservation.getCreateTime());
        dto.setReservationDate(reservation.getReservationDate());
        dto.setStatus(reservation.getStatus());
        dto.setFlightClass(reservation.getFlightClass());
        dto.setPassengerType(reservation.getPassengerType());
        dto.setBaggageOption(reservation.getBaggageOption());
        dto.setWifiOption(reservation.getWifiOption());
        dto.setEntertainmentOption(reservation.getEntertainmentOption());
        dto.setTotalPrice(reservation.getTotalPrice());

        if (reservation.getFlight() != null) {
            dto.setFlightId(reservation.getFlight().getId());
            dto.setFlightNum(reservation.getFlight().getFlightNo());
        }

        if (reservation.getSeat() != null) {
            dto.setSeatId(reservation.getSeat().getId());
            dto.setSeatNumber(reservation.getSeat().getSeatNumber());
        }

        if (reservation.getPassenger() != null) {
            dto.setPassengerId(reservation.getPassenger().getId());
            dto.setPassengerFullName(
                    reservation.getPassenger().getFirstName() + " " + reservation.getPassenger().getLastName()
            );
        }

        if (reservation.getUser() != null) {
            dto.setUserId(reservation.getUser().getId());
            dto.setUserEmail(reservation.getUser().getEmail());
        }

        return dto;
    }
}
