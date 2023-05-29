package com.joejoe2.demo.repository.jwt;

import com.joejoe2.demo.model.auth.AccessToken;
import com.joejoe2.demo.model.auth.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID> {

  Optional<AccessToken> getByIdAndExpireAtGreaterThan(UUID id, Instant dateTime);

  List<AccessToken> getByUser(User user);
}
