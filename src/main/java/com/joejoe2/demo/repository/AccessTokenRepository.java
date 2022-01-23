package com.joejoe2.demo.repository;

import com.joejoe2.demo.model.AccessToken;
import com.joejoe2.demo.model.RefreshToken;
import com.joejoe2.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID> {
    void deleteByExpireAtLessThan(LocalDateTime dateTime);
    Optional<AccessToken> getByTokenAndExpireAtGreaterThan(String token, LocalDateTime dateTime);
    List<AccessToken> getByUser(User user);
}
