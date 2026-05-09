package com.example.ucakbiletotamasyonu.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "ticket")
public class Ticket extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String ticketNumber;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

}
