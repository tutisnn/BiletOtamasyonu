package com.example.ucakbiletotamasyonu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketDto extends DtoBase {

    private Integer reservationId;
    private String ticketNumber;
    private String flightNum;
    private String passengerFullName;
    private String seatNumber;
}
