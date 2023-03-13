package com.joejoe2.demo.repository.jwt;

import com.joejoe2.demo.model.auth.AccessToken;
import com.joejoe2.demo.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID> {
    Optional<AccessToken> getByTokenAndExpireAtGreaterThan(String token, Instant dateTime);

    List<AccessToken> getByUser(User user);
}
