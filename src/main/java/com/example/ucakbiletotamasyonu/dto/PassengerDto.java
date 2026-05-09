package com.example.ucakbiletotamasyonu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDto {

    private Integer id;
    private String firstName;
    private String lastName;
    private String identityNumber;
    private String passportNumber;
    private LocalDate birthDate;
    private String gender;
}
