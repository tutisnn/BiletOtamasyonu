package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.model.RefreshToken;
import com.example.ucakbiletotamasyonu.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    public Optional<RefreshToken> findByRefreshToken(String refreshToken);

    @Transactional
    void deleteByRefreshToken(String refreshToken);

    @Transactional
    void deleteByUser(User user);
}
