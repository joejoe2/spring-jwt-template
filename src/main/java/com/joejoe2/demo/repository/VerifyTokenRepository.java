package com.joejoe2.demo.repository;

import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.model.auth.VerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface VerifyTokenRepository extends JpaRepository<VerifyToken, UUID> {
    void deleteByExpireAtLessThan(LocalDateTime dateTime);
    Optional<VerifyToken> getByTokenAndExpireAtGreaterThan(String token, LocalDateTime dateTime);
    Optional<VerifyToken> getByUser(User user);
}
