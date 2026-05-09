package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
 @Repository
public interface UserRepository extends JpaRepository<User, Integer> { //changed long to ınteger. change if you want to
    Optional<User> findByEmail(String email);
}

