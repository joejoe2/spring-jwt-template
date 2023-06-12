package com.joejoe2.demo.model.auth;

import com.joejoe2.demo.model.Base;
import java.time.Instant;
import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
public class VerifyToken extends Base {
  @Column(unique = true, updatable = false, nullable = false, columnDefinition = "TEXT")
  private String token;

  @Column(updatable = false, nullable = false)
  private Instant expireAt;

  @OneToOne
  @JoinColumn(unique = true) // unidirectional one to one
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;
}
