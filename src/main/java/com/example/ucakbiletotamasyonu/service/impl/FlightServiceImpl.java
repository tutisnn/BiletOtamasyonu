package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.dto.FlightDto;
import com.example.ucakbiletotamasyonu.dto.AirportOptionDto;
import com.example.ucakbiletotamasyonu.enums.FlightClass;
import com.example.ucakbiletotamasyonu.enums.SeatStatus;
import com.example.ucakbiletotamasyonu.exception.BaseException;
import com.example.ucakbiletotamasyonu.exception.ErrorMessage;
import com.example.ucakbiletotamasyonu.exception.MessageType;
import com.example.ucakbiletotamasyonu.mapper.FlightMapper;
import com.example.ucakbiletotamasyonu.model.AirportInfo;
import com.example.ucakbiletotamasyonu.model.Flight;
import com.example.ucakbiletotamasyonu.model.Seat;
import com.example.ucakbiletotamasyonu.repository.FlightRepository;
import com.example.ucakbiletotamasyonu.repository.SeatRepository;
import com.example.ucakbiletotamasyonu.service.IFlightService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
public class FlightServiceImpl implements IFlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private FlightMapper flightMapper;

    @Override
    public FlightDto saveFlight(FlightDto flightDto) {
        if (flightDto == null) {
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Flight data cannot be null"));
        }

        Optional<Flight> existingFlight = flightRepository.findByFlightNoAndDeletedFalse(flightDto.getFlightNo());
        if (existingFlight.isPresent()) {
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION,
                    "Flight already exists with flight number: " + flightDto.getFlightNo()));
        }

        Flight flight = flightMapper.dtoToFlight(flightDto);
        int totalSeatCount = 144;
        flight.setCapacity(totalSeatCount);
        flight.setAvailableSeats(totalSeatCount);

        Flight savedFlight = flightRepository.save(flight);
        List<Seat> seats = generateSeatsForFlight(savedFlight);
        seatRepository.saveAll(seats);
        savedFlight.setSeats(seats);

        return flightMapper.flightToDto(savedFlight);
    }

    @Override
    public List<FlightDto> getAllFlights() {
        List<Flight> flights = flightRepository.findByDeletedFalse();
        return flights.stream()
                .map(flightMapper::flightToDto)
                .toList();
    }

    @Override
    public FlightDto getFlightById(Integer id) {
        Flight flight = flightRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.NO_RECORD_EXIST, "Flight not found with id: " + id)));

        return flightMapper.flightToDto(flight);
    }

    @Override
    public List<FlightDto> searchFlights(String departureCity, String arrivalCity, LocalDate departureDate,
                                         String departureAirport, String arrivalAirport) {
        List<Flight> flights;
        if (departureAirport != null && !departureAirport.isBlank()
                && arrivalAirport != null && !arrivalAirport.isBlank()) {
            flights = flightRepository.findByDeparture_CityAndDeparture_AirportAndArrival_CityAndArrival_AirportAndDeletedFalse(
                    departureCity, departureAirport, arrivalCity, arrivalAirport
            );
        } else {
            flights = flightRepository.findByDeparture_CityAndArrival_CityAndDeletedFalse(departureCity, arrivalCity);
        }

        return flights.stream()
                .filter(flight -> flight.getDepartureTime() != null)
                .filter(flight -> flight.getDepartureTime().toLocalDate().equals(departureDate))
                .map(flightMapper::flightToDto)
                .toList();
    }

    @Override
    public void deleteFlightById(Integer id) {
        Flight flight = flightRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.NO_RECORD_EXIST, "Flight not found with id: " + id)));
        flight.setDeleted(true);
        flight.setStatus(com.example.ucakbiletotamasyonu.enums.FlightStatus.CANCELLED);
        flightRepository.save(flight);
    }

    @Override
    public FlightDto updateFlight(Integer id, FlightDto updatedFlightDto) {
        Flight flight = flightRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BaseException(new ErrorMessage(MessageType.NO_RECORD_EXIST, "Flight not found with id: " + id)));

        flight.setFlightNo(updatedFlightDto.getFlightNo());
        flight.setAirline(updatedFlightDto.getAirline());
        flight.setDeparture(toAirportInfo(updatedFlightDto.getDeparture()));
        flight.setArrival(toAirportInfo(updatedFlightDto.getArrival()));
        flight.setDepartureTime(updatedFlightDto.getDepartureTime());
        flight.setArrivalTime(updatedFlightDto.getArrivalTime());
        flight.setPrice(updatedFlightDto.getPrice());
        flight.setStatus(updatedFlightDto.getStatus());

        Flight updatedFlight = flightRepository.save(flight);
        return flightMapper.flightToDto(updatedFlight);
    }

    @Override
    public List<AirportOptionDto> getAirportOptions() {
        Map<String, AirportOptionDto> dedup = new LinkedHashMap<>();

        for (Object[] row : flightRepository.findDistinctDepartureAirportOptions()) {
            AirportOptionDto dto = toAirportOption(row);
            if (dto != null) {
                dedup.put(key(dto), dto);
            }
        }
        for (Object[] row : flightRepository.findDistinctArrivalAirportOptions()) {
            AirportOptionDto dto = toAirportOption(row);
            if (dto != null) {
                dedup.put(key(dto), dto);
            }
        }

        return new ArrayList<>(dedup.values());
    }

    private AirportOptionDto toAirportOption(Object[] row) {
        if (row == null || row.length < 2) {
            return null;
        }
        String city = row[0] == null ? null : row[0].toString();
        String airport = row[1] == null ? null : row[1].toString();
        if (city == null || city.isBlank() || airport == null || airport.isBlank()) {
            return null;
        }
        return new AirportOptionDto(city, airport);
    }

    private String key(AirportOptionDto dto) {
        return dto.getCity().trim().toLowerCase() + "|" + dto.getAirport().trim().toLowerCase();
    }

    private AirportInfo toAirportInfo(com.example.ucakbiletotamasyonu.dto.AirportInfoDto dto) {
        if (dto == null) {
            return null;
        }
        AirportInfo info = new AirportInfo();
        info.setCity(dto.getCity());
        info.setAirport(dto.getAirport());
        return info;
    }

    private List<Seat> generateSeatsForFlight(Flight flight) {
        List<Seat> seats = new ArrayList<>();

        seats.addAll(generateFirstClassSeats(flight));
        seats.addAll(generateBusinessClassSeats(flight));
        seats.addAll(generateEconomyClassSeats(flight));

        return seats;
    }

    private List<Seat> generateFirstClassSeats(Flight flight) {
        List<Seat> seats = new ArrayList<>();

        int[] rows = {1, 2};
        char[] cols = {'A', 'B'};

        for (int row : rows) {
            for (char col : cols) {
                seats.add(createSeat(flight, row + String.valueOf(col), FlightClass.FIRST_CLASS));
            }
        }

        return seats;
    }

    private List<Seat> generateBusinessClassSeats(Flight flight) {
        List<Seat> seats = new ArrayList<>();

        for (int row = 3; row <= 7; row++) {
            for (char col : new char[]{'A', 'B', 'C', 'D'}) {
                seats.add(createSeat(flight, row + String.valueOf(col), FlightClass.BUSINESS));
            }
        }

        return seats;
    }

    private List<Seat> generateEconomyClassSeats(Flight flight) {
        List<Seat> seats = new ArrayList<>();

        for (int row = 8; row <= 27; row++) {
            for (char col : new char[]{'A', 'B', 'C', 'D', 'E', 'F'}) {
                seats.add(createSeat(flight, row + String.valueOf(col), FlightClass.ECONOMY));
            }
        }

        return seats;
    }

    private Seat createSeat(Flight flight, String seatNumber, FlightClass flightClass) {
        Seat seat = new Seat();
        seat.setFlight(flight);
        seat.setSeatNumber(seatNumber);
        seat.setFlightClass(flightClass);
        seat.setStatus(SeatStatus.AVAILABLE);
        return seat;
    }
}
