package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.enums.ReservationStatus;
import com.example.ucakbiletotamasyonu.model.Reservation;
import com.example.ucakbiletotamasyonu.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByUser(User user);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByUserAndStatus(User user, ReservationStatus status);
}
