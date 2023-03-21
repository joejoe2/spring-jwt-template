package com.joejoe2.demo.data.auth;

import com.joejoe2.demo.validation.constraint.UUID;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VerificationPair {
  @UUID(message = "invalid verification key !")
  @NotEmpty(message = "verification key cannot be empty !")
  private String key;

  @NotEmpty(message = "verification code cannot be empty !")
  private String code;

  public VerificationPair(String key, String verificationCode) {
    this.key = key;
    this.code = verificationCode;
  }
}
