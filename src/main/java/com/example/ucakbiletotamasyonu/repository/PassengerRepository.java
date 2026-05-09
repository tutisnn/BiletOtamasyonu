package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Integer> {
}
