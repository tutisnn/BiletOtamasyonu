package com.example.ucakbiletotamasyonu.model;

import com.example.ucakbiletotamasyonu.enums.Airline;
import com.example.ucakbiletotamasyonu.enums.FlightStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Getter
@Setter
@Table(name="flight")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column( nullable=false, unique = true)
    private String flightNo;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city", column = @Column(name = "departure_city", nullable = false)),
            @AttributeOverride(name = "airport", column = @Column(name = "departure_airport", nullable = false))
    })
    private AirportInfo departure;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city", column = @Column(name = "arrival_city", nullable = false)),
            @AttributeOverride(name = "airport", column = @Column(name = "arrival_airport", nullable = false))
    })
    private AirportInfo arrival;

    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime departureTime;

    @Column( nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime arrivalTime;

    @Column( nullable = false)
    private BigDecimal price;

    @Column( nullable = false)
    private Integer capacity;

    @Column( nullable = false)
    private Integer availableSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Airline airline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;

    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "flight", cascade=CascadeType.ALL)
    private List<Seat> seats;
}
