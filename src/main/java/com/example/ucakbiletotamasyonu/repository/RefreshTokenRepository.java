package com.example.ucakbiletotamasyonu.repository;

import com.example.ucakbiletotamasyonu.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    public Optional<RefreshToken> findByRefreshToken(String refreshToken);

    @Transactional
    void deleteByRefreshToken(String refreshToken);

}

