package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.enums.PaymentStatus;
import com.example.ucakbiletotamasyonu.model.Payment;
import com.example.ucakbiletotamasyonu.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByReservation(Reservation reservation);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByStripeSessionId(String stripeSessionId);
}
