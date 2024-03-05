package com.joejoe2.demo.model.auth;

import com.fasterxml.uuid.Generators;
import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.utils.JwtUtil;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
  @Id
  @Column(unique = true, updatable = false, nullable = false)
  private UUID id = Generators.timeBasedEpochGenerator().generate();

  @Column(unique = true, updatable = false, nullable = false, columnDefinition = "TEXT")
  private String token;

  @Column(updatable = false, nullable = false)
  private Instant expireAt;

  @OneToOne(mappedBy = "refreshToken", cascade = CascadeType.ALL)
  // cascade delete not work for jpql/sql, so we use @OnDelete on child !
  private AccessToken accessToken;

  @ManyToOne(optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  public RefreshToken(JwtConfig jwtConfig, AccessToken accessToken) {
    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.SECOND, jwtConfig.getRefreshTokenLifetimeSec());
    this.token =
        JwtUtil.generateRefreshToken(
            jwtConfig.getPrivateKey(), getId().toString(), jwtConfig.getIssuer(), exp);
    accessToken.setRefreshToken(this);
    this.accessToken = accessToken;
    this.user = accessToken.getUser();
    this.expireAt = exp.toInstant();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RefreshToken that = (RefreshToken) o;
    return id.equals(that.id) && token.equals(that.token) && expireAt.equals(that.expireAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, token, expireAt);
  }
}
