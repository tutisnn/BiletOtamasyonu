package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.ResponseMessage.Constants;
import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
import com.example.ucakbiletotamasyonu.dto.ReservationCreateDto;
import com.example.ucakbiletotamasyonu.dto.ReservationDto;
import com.example.ucakbiletotamasyonu.enums.PassengerType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationServiceImpl implements IReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

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
        if (reservationCreateDto == null) {
            log.warn("saveReservation called with null dto");
            return GenericResponse.error("Invalid reservation request");
        }

        log.info("saveReservation called flightId={}, seatId={}, flightClass={}, passengerType={}, baggage={}, wifi={}, entertainment={}",
                reservationCreateDto.getFlightId(),
                reservationCreateDto.getSeatId(),
                reservationCreateDto.getFlightClass(),
                reservationCreateDto.getPassengerType(),
                reservationCreateDto.getBaggageOption(),
                reservationCreateDto.getWifiOption(),
                reservationCreateDto.getEntertainmentOption());

        Flight flight = flightRepository.findById(reservationCreateDto.getFlightId()).orElse(null);
        Seat seat = seatRepository.findById(reservationCreateDto.getSeatId()).orElse(null);
        User user = resolveAuthenticatedUser();

        if (flight == null) return GenericResponse.error(Constants.EMPTY_FLIGHT);
        if (seat == null) return GenericResponse.error(Constants.EMPTY_SEAT);
        if (user == null) return GenericResponse.error(Constants.EMPTY_USER);

        log.info("saveReservation loaded flightId={}, flightNo={}, seatId={}, seatNumber={}, seatStatus={}, seatClass={}, userEmail={}",
                flight.getId(),
                flight.getFlightNo(),
                seat.getId(),
                seat.getSeatNumber(),
                seat.getStatus(),
                seat.getFlightClass(),
                user.getEmail());

        if (seat.getFlight() == null || !seat.getFlight().getId().equals(flight.getId())) {
            log.warn("saveReservation rejected INVALID_SEAT_FLIGHT: seatFlightId={}, requestFlightId={}",
                    seat.getFlight() == null ? null : seat.getFlight().getId(),
                    flight.getId());
            return GenericResponse.error(Constants.INVALID_SEAT_FLIGHT);
        }

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            log.warn("saveReservation rejected SEAT_ALREADY_RESERVED: seatStatus={}", seat.getStatus());
            return GenericResponse.error(Constants.SEAT_ALREADY_RESERVED);
        }

        if (flight.getDeparture() != null
                && flight.getArrival() != null
                && flight.getDeparture().getCity() != null
                && flight.getArrival().getCity() != null
                && flight.getDeparture().getCity().equalsIgnoreCase(flight.getArrival().getCity())
                && flight.getDeparture().getAirport() != null
                && flight.getArrival().getAirport() != null
                && flight.getDeparture().getAirport().equalsIgnoreCase(flight.getArrival().getAirport())) {
            log.warn("saveReservation rejected INVALID_ROUTE: departureCity={}, departureAirport={}, arrivalCity={}, arrivalAirport={}",
                    flight.getDeparture().getCity(),
                    flight.getDeparture().getAirport(),
                    flight.getArrival().getCity(),
                    flight.getArrival().getAirport());
            return GenericResponse.error(Constants.INVALID_ROUTE);
        }

        if (reservationCreateDto.getFlightClass() == null) {
            log.warn("saveReservation rejected INVALID_SEAT_CLASS: request flightClass is null");
            return GenericResponse.error(Constants.INVALID_SEAT_CLASS);
        }

        if (seat.getFlightClass() != reservationCreateDto.getFlightClass()) {
            log.warn("saveReservation rejected INVALID_SEAT_CLASS: seatClass={}, requestClass={}",
                    seat.getFlightClass(),
                    reservationCreateDto.getFlightClass());
            return GenericResponse.error(Constants.INVALID_SEAT_CLASS);
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
        reservation.setPassengerType(
                reservationCreateDto.getPassengerType() != null
                        ? reservationCreateDto.getPassengerType()
                        : PassengerType.ADULT
        );
        reservation.setBaggageOption(reservationCreateDto.getBaggageOption());
        reservation.setWifiOption(reservationCreateDto.getWifiOption());
        reservation.setEntertainmentOption(reservationCreateDto.getEntertainmentOption());

        BigDecimal totalPrice = calculateTotalPrice(flight.getPrice(), reservationCreateDto);
        reservation.setTotalPrice(totalPrice);
        log.info("saveReservation calculated totalPrice={}, basePrice={}", totalPrice, flight.getPrice());

        seat.setStatus(SeatStatus.RESERVED);
        seatRepository.save(seat);
        decreaseAvailableSeats(flight);

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("saveReservation success reservationId={}, status={}, seatId={}, seatStatus={}, flightId={}, availableSeats={}",
                savedReservation.getId(),
                savedReservation.getStatus(),
                seat.getId(),
                seat.getStatus(),
                flight.getId(),
                flight.getAvailableSeats());
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

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            return GenericResponse.error(Constants.ONLY_PENDING_CAN_BE_DELETED);
        }

        if (reservation.getSeat() != null) {
            reservation.getSeat().setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(reservation.getSeat());
            increaseAvailableSeats(reservation.getFlight());
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

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            return GenericResponse.error(Constants.ONLY_PENDING_CAN_BE_CANCELLED);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        if (reservation.getSeat() != null) {
            reservation.getSeat().setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(reservation.getSeat());
            increaseAvailableSeats(reservation.getFlight());
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
        PassengerType passengerType = dto.getPassengerType() != null ? dto.getPassengerType() : PassengerType.ADULT;
        BigDecimal totalPrice = applyPassengerTypePrice(basePrice, passengerType);

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

    private BigDecimal applyPassengerTypePrice(BigDecimal basePrice, PassengerType passengerType) {
        return switch (passengerType) {
            case ADULT -> basePrice;
            case CHILD -> basePrice.multiply(BigDecimal.valueOf(0.50));
            case STUDENT -> basePrice.multiply(BigDecimal.valueOf(0.80));
        };
    }

    private void decreaseAvailableSeats(Flight flight) {
        if (flight == null || flight.getAvailableSeats() == null) {
            return;
        }

        if (flight.getAvailableSeats() > 0) {
            flight.setAvailableSeats(flight.getAvailableSeats() - 1);
            flightRepository.save(flight);
        }
    }

    private void increaseAvailableSeats(Flight flight) {
        if (flight == null || flight.getAvailableSeats() == null) {
            return;
        }

        flight.setAvailableSeats(flight.getAvailableSeats() + 1);
        flightRepository.save(flight);
    }

    private User resolveAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            log.warn("saveReservation called without authentication in security context");
            return null;
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}
