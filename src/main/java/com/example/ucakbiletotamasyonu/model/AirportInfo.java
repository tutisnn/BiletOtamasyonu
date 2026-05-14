package com.example.ucakbiletotamasyonu.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class AirportInfo {

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String airport;
}

