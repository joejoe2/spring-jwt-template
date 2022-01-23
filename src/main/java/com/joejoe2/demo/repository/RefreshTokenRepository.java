package com.joejoe2.demo.repository;

import com.joejoe2.demo.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    void deleteByExpireAtLessThan(LocalDateTime dateTime);
    Optional<RefreshToken> getByTokenAndExpireAtGreaterThan(String token, LocalDateTime dateTime);
}
