package com.joejoe2.demo.repository.jwt;

import com.joejoe2.demo.model.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    void deleteByExpireAtLessThan(Instant dateTime);
    Optional<RefreshToken> getByTokenAndExpireAtGreaterThan(String token, Instant dateTime);
}
