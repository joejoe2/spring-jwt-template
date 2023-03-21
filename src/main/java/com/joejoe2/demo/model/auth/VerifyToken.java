package com.joejoe2.demo.model.auth;

import java.time.Instant;
import java.util.UUID;
import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
public class VerifyToken {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(unique = true, updatable = false, nullable = false)
  private UUID id;

  @Column(unique = true, updatable = false, nullable = false, columnDefinition = "TEXT")
  private String token;

  @Column(updatable = false, nullable = false)
  private Instant expireAt;

  @OneToOne
  @JoinColumn(unique = true) // unidirectional one to one
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;
}
