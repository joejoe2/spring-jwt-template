package com.joejoe2.demo.service.user.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.config.LoginConfig;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class LoginServiceTest {
  @Autowired LoginConfig loginConfig;
  @Autowired UserRepository userRepository;
  @Autowired LoginService loginService;
  @Autowired PasswordEncoder passwordEncoder;

  User user, incative;

  @BeforeEach
  void setUp() {
    loginConfig.setMaxAttempts(2);
    loginConfig.setCoolTime(15);

    user = new User();
    user.setUserName("test");
    user.setPassword(passwordEncoder.encode("pa55ward"));
    user.setEmail("test@email.com");
    userRepository.save(user);
    incative = new User();
    incative.setActive(false);
    incative.setUserName("incative");
    incative.setPassword(passwordEncoder.encode("pa55ward"));
    incative.setEmail("incative@email.com");
    userRepository.save(incative);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteById(user.getId());
    userRepository.deleteById(incative.getId());
  }

  @Test
  void login() throws Exception {
    assertThrows(
        BadCredentialsException.class,
        () -> {
          loginService.login(user.getUserName(), "error");
        });
    assertEquals(1, userRepository.findById(user.getId()).get().getLoginAttempt().getAttempts());

    assertThrows(
        BadCredentialsException.class,
        () -> {
          loginService.login(user.getUserName(), "error");
        });
    assertEquals(2, userRepository.findById(user.getId()).get().getLoginAttempt().getAttempts());

    assertThrows(
        AuthenticationException.class,
        () -> {
          loginService.login(user.getUserName(), "error");
        });
    assertEquals(2, userRepository.findById(user.getId()).get().getLoginAttempt().getAttempts());

    assertThrows(
        AuthenticationException.class,
        () -> {
          loginService.login(user.getUserName(), "pa55ward");
        });
    assertEquals(2, userRepository.findById(user.getId()).get().getLoginAttempt().getAttempts());

    Thread.sleep(loginConfig.getCoolTime() * 1000L);

    assertThrows(
        BadCredentialsException.class,
        () -> {
          loginService.login(user.getUserName(), "error");
        });
    assertEquals(1, userRepository.findById(user.getId()).get().getLoginAttempt().getAttempts());

    loginService.login(user.getUserName(), "pa55ward");
    assertEquals(0, userRepository.findById(user.getId()).get().getLoginAttempt().getAttempts());

    assertThrows(
        BadCredentialsException.class,
        () -> {
          loginService.login(user.getUserName(), "error");
        });
    assertEquals(1, userRepository.findById(user.getId()).get().getLoginAttempt().getAttempts());

    assertThrows(
        BadCredentialsException.class,
        () -> {
          loginService.login(user.getUserName(), "error");
        });
    assertEquals(2, userRepository.findById(user.getId()).get().getLoginAttempt().getAttempts());

    assertThrows(
        AuthenticationException.class,
        () -> {
          loginService.login(user.getUserName(), "error");
        });
    assertEquals(2, userRepository.findById(user.getId()).get().getLoginAttempt().getAttempts());

    assertThrows(
        AuthenticationException.class,
        () -> {
          loginService.login(user.getUserName(), "pa55ward");
        });
    assertEquals(2, userRepository.findById(user.getId()).get().getLoginAttempt().getAttempts());
  }

  @Test
  void loginInactive() {
    // inactive user cannot login, will never increase attempts
    assertThrows(
        AuthenticationException.class,
        () -> {
          loginService.login(incative.getUserName(), "error");
        });
    assertEquals(
        0, userRepository.findById(incative.getId()).get().getLoginAttempt().getAttempts());

    assertThrows(
        AuthenticationException.class,
        () -> {
          loginService.login(incative.getUserName(), "pa55ward");
        });
    assertEquals(
        0, userRepository.findById(incative.getId()).get().getLoginAttempt().getAttempts());
  }
}
