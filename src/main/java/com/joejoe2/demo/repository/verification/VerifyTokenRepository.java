package com.joejoe2.demo.repository.verification;

import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.model.auth.VerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface VerifyTokenRepository extends JpaRepository<VerifyToken, UUID> {
    void deleteByExpireAtLessThan(Instant dateTime);
    Optional<VerifyToken> getByTokenAndExpireAtGreaterThan(String token, Instant dateTime);
    Optional<VerifyToken> getByUser(User user);
    void deleteByUser(User user);
}
