package com.joejoe2.demo.model.auth;

import com.joejoe2.demo.model.Base;
import com.joejoe2.demo.utils.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.Instant;
import lombok.Data;

@Data
@Entity
public class VerificationCode extends Base {
  @Column(length = 128, nullable = false)
  private String email;

  @Column(length = 5, nullable = false)
  private String code = Utils.randomNumericCode(5);

  @Column(updatable = false, nullable = false)
  private Instant expireAt;
}
