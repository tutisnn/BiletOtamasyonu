package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.model.PasswordResetToken;
import com.example.ucakbiletotamasyonu.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    @Transactional
    void deleteByUser(User user);
}
