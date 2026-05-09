package com.example.ucakbiletotamasyonu.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Where(clause = "deleted=false")
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

    @Column(nullable = false)
    private LocalDate birthDate;

    private String gender;

    private boolean deleted = false;
}