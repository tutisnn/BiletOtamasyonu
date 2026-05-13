package com.example.ucakbiletotamasyonu.ResponseMessage;

public class Constants {

    public static final String EMPTY_ID = "No registration found with the same id";
    public static final String EMPTY_LIST = "No registration found";

    public static final String EMPTY_FLIGHT = "Flight not found";
    public static final String EMPTY_SEAT = "Seat not found";
    public static final String EMPTY_PASSENGER = "Passenger not found";
    public static final String EMPTY_RESERVATION = "Reservation not found";
    public static final String EMPTY_PAYMENT = "Payment not found";
    public static final String EMPTY_TICKET = "Ticket not found";
    public static final String EMPTY_USER = "User not found";

    public static final String FOUND_FLIGHT = "Flight found with the same id";
    public static final String FOUND_SEAT = "Seat found with the same id";
    public static final String FOUND_PASSENGER = "Passenger found with the same id";
    public static final String FOUND_RESERVATION = "Reservation found with the same id";
    public static final String FOUND_PAYMENT = "Payment found with the same id";
    public static final String FOUND_TICKET = "Ticket found with the same id";
    public static final String FOUND_USER = "User found with the same id";

    public static final String FLIGHT_ALREADY_EXISTS = "Flight already exists";
    public static final String SEAT_ALREADY_RESERVED = "Seat is already reserved";
    public static final String PAYMENT_ALREADY_EXISTS = "Payment already exists for this reservation";
    public static final String TICKET_ALREADY_EXISTS = "Ticket already exists for this reservation";

    public static final String FLIGHT_SAVED = "Flight saved successfully";
    public static final String FLIGHT_UPDATED = "Flight updated successfully";
    public static final String FLIGHT_DELETED = "Flight deleted successfully";

    public static final String RESERVATION_SAVED = "Reservation created successfully";
    public static final String RESERVATION_UPDATED = "Reservation updated successfully";
    public static final String RESERVATION_CANCELLED = "Reservation cancelled successfully";

    public static final String PAYMENT_SAVED = "Payment completed successfully";
    public static final String TICKET_CREATED = "Ticket created successfully";

    public static final String INVALID_ROUTE = "Departure and arrival cannot be the same";
    public static final String INVALID_DATE = "Invalid flight date";
    public static final String INVALID_PAYMENT = "Payment information is invalid";
    public static final String INVALID_SEAT_CLASS = "Selected seat does not match selected flight class";
    public static final String INVALID_SEAT_FLIGHT = "Selected seat does not belong to the selected flight";
    public static final String CONFIRMED_RESERVATION_DELETE_ERROR = "Confirmed reservation cannot be deleted";
    public static final String CONFIRMED_RESERVATION_CANCEL_ERROR = "Confirmed reservation cannot be cancelled";
    public static final String ONLY_PENDING_CAN_BE_CANCELLED = "Only pending reservations can be cancelled";
    public static final String ONLY_PENDING_CAN_BE_DELETED = "Only pending reservations can be deleted";
    public static final String ONLY_PENDING_PAYMENT_CAN_BE_CANCELLED = "Only pending reservation payments can be cancelled";

    private Constants() {
    }
}
