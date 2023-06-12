package com.joejoe2.demo.model.auth;

import com.fasterxml.uuid.Generators;
import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.utils.JwtUtil;
import java.time.Instant;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {
  @Id
  @Column(unique = true, updatable = false, nullable = false)
  private UUID id = Generators.timeBasedEpochGenerator().generate();

  @Column(unique = true, updatable = false, nullable = false, columnDefinition = "TEXT")
  private String token;

  @Column(updatable = false, nullable = false)
  private Instant expireAt;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE) // if delete refreshToken => also delete this
  RefreshToken refreshToken;

  @ManyToOne(optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  public AccessToken(JwtConfig jwtConfig, User user) {
    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.SECOND, jwtConfig.getAccessTokenLifetimeSec());
    this.token =
        JwtUtil.generateAccessToken(
            jwtConfig.getPrivateKey(), getId().toString(), jwtConfig.getIssuer(), user, exp);
    this.expireAt = exp.toInstant();
    this.user = user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AccessToken that = (AccessToken) o;
    return id.equals(that.id) && token.equals(that.token) && expireAt.equals(that.expireAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, token, expireAt);
  }
}
