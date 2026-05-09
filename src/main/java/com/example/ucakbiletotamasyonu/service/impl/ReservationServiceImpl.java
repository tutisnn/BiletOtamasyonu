package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.ResponseMessage.Constants;
import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
import com.example.ucakbiletotamasyonu.dto.ReservationCreateDto;
import com.example.ucakbiletotamasyonu.dto.ReservationDto;
import com.example.ucakbiletotamasyonu.enums.ReservationStatus;
import com.example.ucakbiletotamasyonu.enums.SeatStatus;
import com.example.ucakbiletotamasyonu.mapper.ReservationMapper;
import com.example.ucakbiletotamasyonu.model.Flight;
import com.example.ucakbiletotamasyonu.model.Passenger;
import com.example.ucakbiletotamasyonu.model.Reservation;
import com.example.ucakbiletotamasyonu.model.Seat;
import com.example.ucakbiletotamasyonu.model.User;
import com.example.ucakbiletotamasyonu.repository.FlightRepository;
import com.example.ucakbiletotamasyonu.repository.PassengerRepository;
import com.example.ucakbiletotamasyonu.repository.ReservationRepository;
import com.example.ucakbiletotamasyonu.repository.SeatRepository;
import com.example.ucakbiletotamasyonu.repository.UserRepository;
import com.example.ucakbiletotamasyonu.service.IReservationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationServiceImpl implements IReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    public GenericResponse<?> saveReservation(ReservationCreateDto reservationCreateDto) {
        Flight flight = flightRepository.findById(reservationCreateDto.getFlightId()).orElse(null);
        Seat seat = seatRepository.findById(reservationCreateDto.getSeatId()).orElse(null);
        User user = userRepository.findById(reservationCreateDto.getUserId()).orElse(null);

        if (flight == null) return GenericResponse.error(Constants.EMPTY_FLIGHT);
        if (seat == null) return GenericResponse.error(Constants.EMPTY_SEAT);
        if (user == null) return GenericResponse.error(Constants.EMPTY_USER);

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            return GenericResponse.error(Constants.SEAT_ALREADY_RESERVED);
        }

        if (flight.getDeparture().equalsIgnoreCase(flight.getArrival())) {
            return GenericResponse.error(Constants.INVALID_ROUTE);
        }

        Passenger passenger = new Passenger();
        passenger.setFirstName(reservationCreateDto.getFirstName());
        passenger.setLastName(reservationCreateDto.getLastName());
        passenger.setIdentityNumber(reservationCreateDto.getIdentityNumber());
        passenger.setPassportNumber(reservationCreateDto.getPassportNumber());
        passenger.setEmail(reservationCreateDto.getEmail());
        passenger.setPhoneNumber(reservationCreateDto.getPhoneNumber());

        Passenger savedPassenger = passengerRepository.save(passenger);

        Reservation reservation = new Reservation();
        reservation.setFlight(flight);
        reservation.setSeat(seat);
        reservation.setUser(user);
        reservation.setPassenger(savedPassenger);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setFlightClass(reservationCreateDto.getFlightClass());
        reservation.setBaggageOption(reservationCreateDto.getBaggageOption());
        reservation.setWifiOption(reservationCreateDto.getWifiOption());
        reservation.setEntertainmentOption(reservationCreateDto.getEntertainmentOption());
        reservation.setTotalPrice(calculateTotalPrice(flight.getPrice(), reservationCreateDto));

        seat.setStatus(SeatStatus.RESERVED);
        seatRepository.save(seat);

        Reservation savedReservation = reservationRepository.save(reservation);
        return GenericResponse.success(reservationMapper.reservationToDto(savedReservation));
    }

    @Override
    public GenericResponse<?> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return GenericResponse.success(
                reservations.stream().map(reservationMapper::reservationToDto).toList()
        );
    }

    @Override
    public GenericResponse<?> getReservationById(Integer id) {
        return reservationRepository.findById(id)
                .map(reservation -> GenericResponse.success(reservationMapper.reservationToDto(reservation)))
                .orElseGet(() -> GenericResponse.error(Constants.EMPTY_RESERVATION));
    }

    @Override
    public GenericResponse<?> getReservationsByUserId(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return GenericResponse.error(Constants.EMPTY_USER);

        List<Reservation> reservations = reservationRepository.findByUser(user);
        return GenericResponse.success(
                reservations.stream().map(reservationMapper::reservationToDto).toList()
        );
    }

    @Override
    public GenericResponse<?> deleteReservationById(Integer id) {
        Optional<Reservation> optionalReservation = reservationRepository.findById(id);
        if (optionalReservation.isEmpty()) {
            return GenericResponse.error(Constants.EMPTY_RESERVATION);
        }

        Reservation reservation = optionalReservation.get();

        if (reservation.getSeat() != null) {
            reservation.getSeat().setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(reservation.getSeat());
        }

        reservationRepository.deleteById(id);
        return GenericResponse.success("Reservation deleted successfully");
    }

    @Override
    public GenericResponse<?> cancelReservation(Integer id) {
        Optional<Reservation> optionalReservation = reservationRepository.findById(id);
        if (optionalReservation.isEmpty()) {
            return GenericResponse.error(Constants.EMPTY_RESERVATION);
        }

        Reservation reservation = optionalReservation.get();
        reservation.setStatus(ReservationStatus.CANCELLED);

        if (reservation.getSeat() != null) {
            reservation.getSeat().setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(reservation.getSeat());
        }

        Reservation cancelledReservation = reservationRepository.save(reservation);
        return GenericResponse.success(reservationMapper.reservationToDto(cancelledReservation));
    }

    @Override
    public GenericResponse<?> updateReservation(Integer id, ReservationDto updatedReservationDto) {
        Optional<Reservation> optionalReservation = reservationRepository.findById(id);
        if (optionalReservation.isEmpty()) {
            return GenericResponse.error(Constants.EMPTY_RESERVATION);
        }

        Reservation reservation = optionalReservation.get();
        reservation.setStatus(updatedReservationDto.getStatus());

        Reservation updatedReservation = reservationRepository.save(reservation);
        return GenericResponse.success(reservationMapper.reservationToDto(updatedReservation));
    }

    private BigDecimal calculateTotalPrice(BigDecimal basePrice, ReservationCreateDto dto) {
        BigDecimal totalPrice = basePrice;

        if (dto.getFlightClass() != null) {
            switch (dto.getFlightClass()) {
                case BUSINESS -> totalPrice = totalPrice.add(BigDecimal.valueOf(2625));
                case FIRST_CLASS -> totalPrice = totalPrice.add(BigDecimal.valueOf(5250));
                default -> {
                }
            }
        }

        if (dto.getBaggageOption() != null) {
            switch (dto.getBaggageOption()) {
                case KG_20 -> totalPrice = totalPrice.add(BigDecimal.valueOf(250));
                case KG_30 -> totalPrice = totalPrice.add(BigDecimal.valueOf(450));
                case KG_40 -> totalPrice = totalPrice.add(BigDecimal.valueOf(650));
                default -> {
                }
            }
        }

        if (dto.getWifiOption() != null) {
            switch (dto.getWifiOption()) {
                case BASIC -> totalPrice = totalPrice.add(BigDecimal.valueOf(150));
                case PREMIUM -> totalPrice = totalPrice.add(BigDecimal.valueOf(300));
                default -> {
                }
            }
        }

        if (dto.getEntertainmentOption() != null) {
            switch (dto.getEntertainmentOption()) {
                case BASIC -> totalPrice = totalPrice.add(BigDecimal.valueOf(100));
                case PREMIUM -> totalPrice = totalPrice.add(BigDecimal.valueOf(200));
                default -> {
                }
            }
        }

        return totalPrice;
    }
}
