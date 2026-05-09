package com.example.ucakbiletotamasyonu.mapper;

import com.example.ucakbiletotamasyonu.dto.PassengerDto;
import com.example.ucakbiletotamasyonu.model.Passenger;
import org.springframework.stereotype.Component;

@Component
public class PassengerMapper {

    public Passenger dtoToPassenger(PassengerDto dto) {
        if (dto == null) return null;

        Passenger passenger = new Passenger();
        passenger.setFirstName(dto.getFirstName());
        passenger.setLastName(dto.getLastName());
        passenger.setIdentityNumber(dto.getIdentityNumber());
        passenger.setPassportNumber(dto.getPassportNumber());
        passenger.setGender(dto.getGender());
        return passenger;
    }

    public PassengerDto passengerToDto(Passenger passenger) {
        if (passenger == null) return null;

        PassengerDto dto = new PassengerDto();
        dto.setId(passenger.getId());
        dto.setFirstName(passenger.getFirstName());
        dto.setLastName(passenger.getLastName());
        dto.setIdentityNumber(passenger.getIdentityNumber());
        dto.setPassportNumber(passenger.getPassportNumber());
        dto.setGender(passenger.getGender());
        return dto;
    }
}
