package com.example.ucakbiletotamasyonu.model;

import com.example.ucakbiletotamasyonu.enums.BaggageOption;
import com.example.ucakbiletotamasyonu.enums.FlightClass;
import com.example.ucakbiletotamasyonu.enums.ReservationStatus;
import com.example.ucakbiletotamasyonu.enums.WifiOption;
import com.example.ucakbiletotamasyonu.enums.EntertainmentOption;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "reservation")
public class Reservation extends BaseEntity {
    @Column(nullable = false)
    private LocalDateTime reservationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightClass flightClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BaggageOption baggageOption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WifiOption wifiOption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntertainmentOption entertainmentOption;

    private BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @OneToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;



}