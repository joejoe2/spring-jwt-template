package com.joejoe2.demo.model.auth;

import com.joejoe2.demo.config.LoginConfig;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.utils.AuthUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

@Data
@Embeddable
public class LoginAttempt {
  @Column(nullable = false, columnDefinition = "integer default 0")
  @Setter(AccessLevel.PACKAGE)
  int attempts;

  @Column(nullable = true)
  @Setter(AccessLevel.PACKAGE)
  Instant lastAttempt;

  private boolean isExceedLimit(LoginConfig loginConfig) {
    return getAttempts() >= loginConfig.getMaxAttempts();
  }

  private boolean canAttempt(LoginConfig loginConfig) {
    if (getLastAttempt() != null
        && getLastAttempt().plusSeconds(loginConfig.getCoolTime()).isBefore(Instant.now())) {
      return true;
    }
    return !isExceedLimit(loginConfig);
  }

  private void attempt(LoginConfig loginConfig, boolean success) throws InvalidOperation {
    // check there is too many recent failure attempts
    if (!canAttempt(loginConfig)) throw new InvalidOperation("cannot login anymore !");
    if (success) {
      // clear after success
      setAttempts(0);
    } else {
      // reset older failure attempts before increment
      if (isExceedLimit(loginConfig)) setAttempts(0);
      setAttempts(getAttempts() + 1);
    }
    setLastAttempt(Instant.now());
  }

  public UserDetail login(
      LoginConfig loginConfig,
      AuthenticationManager authenticationManager,
      String username,
      String password)
      throws InvalidOperation {
    try {
      UserDetail userDetail = AuthUtil.authenticate(authenticationManager, username, password);
      attempt(loginConfig, true);
      return userDetail;
    } catch (BadCredentialsException e) {
      attempt(loginConfig, false);
      throw e;
    }
  }
}
