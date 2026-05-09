package com.example.ucakbiletotamasyonu.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "passenger")
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String firstName;

    @Column( nullable = false)
    private String lastName;

    @Column( nullable = false)
    private String identityNumber;

    private String passportNumber;

    private String email;

    private String phoneNumber;

    @Column(nullable = false)
    private LocalDate birthDate;

    private String gender;

    private boolean deleted = false;
}
