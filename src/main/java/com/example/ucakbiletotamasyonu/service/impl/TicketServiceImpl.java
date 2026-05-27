package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.ResponseMessage.Constants;
import com.example.ucakbiletotamasyonu.dto.TicketDto;
import com.example.ucakbiletotamasyonu.enums.PaymentStatus;
import com.example.ucakbiletotamasyonu.enums.ReservationStatus;
import com.example.ucakbiletotamasyonu.exception.BaseException;
import com.example.ucakbiletotamasyonu.exception.ErrorMessage;
import com.example.ucakbiletotamasyonu.exception.MessageType;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public TicketDto createTicketFromReservation(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            throw new BaseException(new ErrorMessage(MessageType.NO_RECORD_EXIST, Constants.EMPTY_RESERVATION));
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Ticket can only be created for confirmed reservation"));
        }

        Payment payment = paymentRepository.findByReservation(reservation).orElse(null);
        if (payment == null) {
            throw new BaseException(new ErrorMessage(MessageType.NO_RECORD_EXIST, Constants.EMPTY_PAYMENT));
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Ticket can only be created after successful payment"));
        }

        Optional<Ticket> existingTicket = ticketRepository.findByReservation(reservation);
        if (existingTicket.isPresent()) {
            return ticketMapper.ticketToDto(existingTicket.get());
        }

        Ticket ticket = new Ticket();
        ticket.setReservation(reservation);
        ticket.setTicketNumber(generateTicketNumber());

        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.ticketToDto(savedTicket);
    }

    @Override
    public List<TicketDto> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream().map(ticketMapper::ticketToDto).toList();
    }

    @Override
    public TicketDto getTicketById(Integer id) {
        return ticketRepository.findById(id)
                .map(ticketMapper::ticketToDto)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.NO_RECORD_EXIST, Constants.EMPTY_TICKET)));
    }

    @Override
    public TicketDto getTicketByReservationId(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            throw new BaseException(new ErrorMessage(MessageType.NO_RECORD_EXIST, Constants.EMPTY_RESERVATION));
        }

        return ticketRepository.findByReservation(reservation)
                .map(ticketMapper::ticketToDto)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.NO_RECORD_EXIST, Constants.EMPTY_TICKET)));
    }

    @Override
    public List<TicketDto> getTicketsByUserId(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new BaseException(new ErrorMessage(MessageType.NO_RECORD_EXIST, Constants.EMPTY_USER));
        }

        List<Reservation> reservations = reservationRepository.findByUser(user);

        List<TicketDto> ticketDtos = reservations.stream()
                .map(ticketRepository::findByReservation)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ticketMapper::ticketToDto)
                .toList();
        return ticketDtos;
    }

    @Override
    public List<TicketDto> getMyTickets() {
        User user = resolveAuthenticatedUser();

        List<Reservation> reservations = reservationRepository.findByUser(user);

        List<TicketDto> ticketDtos = reservations.stream()
                .map(ticketRepository::findByReservation)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ticketMapper::ticketToDto)
                .toList();
        return ticketDtos;
    }

    @Override
    public String deleteTicketById(Integer id) {
        if (!ticketRepository.existsById(id)) {
            throw new BaseException(new ErrorMessage(MessageType.NO_RECORD_EXIST, Constants.EMPTY_TICKET));
        }

        ticketRepository.deleteById(id);
        return "Ticket deleted successfully";
    }

    private String generateTicketNumber() {
        return "TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private User resolveAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new BaseException(new ErrorMessage(MessageType.EMAIL_NOT_FOUND, Constants.EMPTY_USER));
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.EMAIL_NOT_FOUND, authentication.getName())));
    }
}
