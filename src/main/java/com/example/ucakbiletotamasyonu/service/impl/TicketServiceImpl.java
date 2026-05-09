package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.ResponseMessage.Constants;
import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
import com.example.ucakbiletotamasyonu.dto.TicketDto;
import com.example.ucakbiletotamasyonu.enums.PaymentStatus;
import com.example.ucakbiletotamasyonu.enums.ReservationStatus;
import com.example.ucakbiletotamasyonu.mapper.TicketMapper;
import com.example.ucakbiletotamasyonu.model.Payment;
import com.example.ucakbiletotamasyonu.model.Reservation;
import com.example.ucakbiletotamasyonu.model.Ticket;
import com.example.ucakbiletotamasyonu.model.User;
import com.example.ucakbiletotamasyonu.repository.PaymentRepository;
import com.example.ucakbiletotamasyonu.repository.ReservationRepository;
import com.example.ucakbiletotamasyonu.repository.TicketRepository;
import com.example.ucakbiletotamasyonu.repository.UserRepository;
import com.example.ucakbiletotamasyonu.service.ITicketService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TicketServiceImpl implements ITicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketMapper ticketMapper;

    @Override
    public GenericResponse<?> createTicketFromReservation(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return GenericResponse.error(Constants.EMPTY_RESERVATION);
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return GenericResponse.error("Ticket can only be created for confirmed reservation");
        }

        Payment payment = paymentRepository.findByReservation(reservation).orElse(null);
        if (payment == null) {
            return GenericResponse.error(Constants.EMPTY_PAYMENT);
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            return GenericResponse.error("Ticket can only be created after successful payment");
        }

        Optional<Ticket> existingTicket = ticketRepository.findByReservation(reservation);
        if (existingTicket.isPresent()) {
            return GenericResponse.success(ticketMapper.ticketToDto(existingTicket.get()));
        }

        Ticket ticket = new Ticket();
        ticket.setReservation(reservation);
        ticket.setTicketNumber(generateTicketNumber());

        Ticket savedTicket = ticketRepository.save(ticket);
        return GenericResponse.success(ticketMapper.ticketToDto(savedTicket));
    }

    @Override
    public GenericResponse<?> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        return GenericResponse.success(
                tickets.stream().map(ticketMapper::ticketToDto).toList()
        );
    }

    @Override
    public GenericResponse<?> getTicketById(Integer id) {
        return ticketRepository.findById(id)
                .map(ticket -> GenericResponse.success(ticketMapper.ticketToDto(ticket)))
                .orElseGet(() -> GenericResponse.error(Constants.EMPTY_TICKET));
    }

    @Override
    public GenericResponse<?> getTicketByReservationId(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return GenericResponse.error(Constants.EMPTY_RESERVATION);
        }

        return ticketRepository.findByReservation(reservation)
                .map(ticket -> GenericResponse.success(ticketMapper.ticketToDto(ticket)))
                .orElseGet(() -> GenericResponse.error(Constants.EMPTY_TICKET));
    }

    @Override
    public GenericResponse<?> getTicketsByUserId(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return GenericResponse.error(Constants.EMPTY_USER);
        }

        List<Reservation> reservations = reservationRepository.findByUser(user);

        List<TicketDto> ticketDtos = reservations.stream()
                .map(ticketRepository::findByReservation)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ticketMapper::ticketToDto)
                .toList();

        if (ticketDtos.isEmpty()) {
            return GenericResponse.error(Constants.EMPTY_LIST);
        }

        return GenericResponse.success(ticketDtos);
    }

    @Override
    public GenericResponse<?> deleteTicketById(Integer id) {
        if (!ticketRepository.existsById(id)) {
            return GenericResponse.error(Constants.EMPTY_TICKET);
        }

        ticketRepository.deleteById(id);
        return GenericResponse.success("Ticket deleted successfully");
    }

    private String generateTicketNumber() {
        return "TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
