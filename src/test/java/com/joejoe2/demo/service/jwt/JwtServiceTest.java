package com.joejoe2.demo.service.jwt;

import static org.junit.jupiter.api.Assertions.*;

import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.data.auth.AccessTokenSpec;
import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.AccessToken;
import com.joejoe2.demo.model.auth.RefreshToken;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.jwt.AccessTokenRepository;
import com.joejoe2.demo.repository.jwt.RefreshTokenRepository;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.utils.JwtUtil;
import java.time.Instant;
import java.util.Calendar;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class JwtServiceTest {
  @Autowired JwtService jwtService;
  @Autowired private JwtConfig jwtConfig;
  @Autowired UserRepository userRepository;
  @Autowired private AccessTokenRepository accessTokenRepository;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(UUID.randomUUID());
    user.setUserName("test");
    user.setEmail("test@email.com");
    user.setPassword("pa55ward");
    user.setRole(Role.NORMAL);
    userRepository.save(user);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteById(user.getId());
  }

  @Test
  void issueTokensWithUserDoesNotExist() {
    UUID id = UUID.randomUUID();
    while (userRepository.existsById(id)) id = UUID.randomUUID();
    User user = new User();
    user.setId(id);
    user.setUserName("notExists");
    user.setEmail("test@email.com");
    user.setPassword("pa55ward");
    user.setRole(Role.NORMAL);
    // test not exist user
    assertThrows(UserDoesNotExist.class, () -> jwtService.issueTokens(new UserDetail(user)));
  }

  @Test
  @Transactional
  void issueTokens() {
    // test exist user
    assertDoesNotThrow(
        () -> {
          TokenPair tokenPair = jwtService.issueTokens(new UserDetail(user));
          AccessToken accessToken = tokenPair.getAccessToken();
          RefreshToken refreshToken = tokenPair.getRefreshToken();
          assertEquals(accessToken, accessTokenRepository.getById(accessToken.getId()));
          assertEquals(refreshToken, refreshTokenRepository.getById(refreshToken.getId()));
          assertEquals(accessToken.getUser(), user);
          assertEquals(refreshToken.getUser(), user);
          assertEquals(refreshToken.getAccessToken(), accessToken);
        });
  }

  @SpyBean JwtService spyService;

  @Test
  void refreshTokensWithInvalidToken() {
    // test invalid token
    assertThrows(InvalidTokenException.class, () -> jwtService.refreshTokens("invalid_token"));
  }

  @Test
  @Transactional
  void refreshTokensWithInactiveUser() throws Exception {
    user.setActive(false);
    TokenPair tokenPair = jwtService.issueTokens(new UserDetail(user));
    RefreshToken refreshToken = tokenPair.getRefreshToken();
    // test InvalidOperation
    assertThrows(InvalidOperation.class, () -> jwtService.refreshTokens(refreshToken.getToken()));
  }

  @Test
  void refreshTokens() {
    // = revokeAccessToken + issueTokens
    assertDoesNotThrow(
        () -> {
          TokenPair tokenPair = jwtService.issueTokens(new UserDetail(user));
          spyService.refreshTokens(tokenPair.getRefreshToken().getToken());
          Mockito.verify(spyService).revokeAccessToken(tokenPair.getAccessToken());
          Mockito.verify(spyService).issueTokens(new UserDetail(user));
        });
  }

  @Test
  void getUserDetailFromAccessTokenWithInvalidToken() {
    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.SECOND, 900);

    // test invalid token
    assertThrows(
        InvalidTokenException.class,
        () -> jwtService.getUserDetailFromAccessToken("invalid_token"));
    assertThrows(
        InvalidTokenException.class,
        () ->
            jwtService.getUserDetailFromAccessToken(
                JwtUtil.generateRefreshToken(jwtConfig.getPrivateKey(), "jti", "iss", exp)));
  }

  @Test
  void getUserDetailFromAccessToken() {
    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.SECOND, 900);

    // test normal token
    String token = JwtUtil.generateAccessToken(jwtConfig.getPrivateKey(), "jti", "iss", user, exp);
    assertDoesNotThrow(
        () -> {
          UserDetail userDetail = jwtService.getUserDetailFromAccessToken(token);
          assertEquals(user.getId().toString(), userDetail.getId());
          assertEquals(user.getUserName(), userDetail.getUsername());
          assertEquals(user.getUserName(), userDetail.getUsername());
          assertEquals(user.isActive(), userDetail.isActive());
          assertEquals(user.getRole(), userDetail.getRole());
        });
  }

  @Test
  void revokeAccessToken() {
    // test valid token
    AccessToken accessToken = new AccessToken();
    accessToken.setToken("test_token");
    accessToken.setUser(user);
    accessToken.setExpireAt(Instant.now().plusSeconds(900));
    RefreshToken refreshToken = new RefreshToken(jwtConfig, accessToken);
    refreshToken.setExpireAt(Instant.now());
    refreshTokenRepository.save(refreshToken);
    assertDoesNotThrow(
        () -> {
          jwtService.revokeAccessToken(accessToken.getId().toString());
          assertTrue(jwtService.isAccessTokenInBlackList(accessToken.getToken()));
        });
  }

  @Test
  void introspect() throws Exception {
    TokenPair tokenPair = jwtService.issueTokens(new UserDetail(user));
    AccessToken accessToken = tokenPair.getAccessToken();
    AccessTokenSpec spec = jwtService.introspect(accessToken.getToken());

    assertEquals(spec.getId(), user.getId().toString());
    assertEquals(spec.getUsername(), user.getUserName());
    assertEquals(spec.getIss(), jwtConfig.getIssuer());
    assertEquals(spec.getJti(), accessToken.getId().toString());
    assertEquals(spec.getType(), "access_token");
    assertEquals(spec.getExp(), accessToken.getExpireAt().getEpochSecond());
    assertEquals(Role.valueOf(spec.getRole()), user.getRole());
    assertEquals(spec.getIsActive(), user.isActive());
  }

  @Test
  void deleteExpiredTokens() {
    AccessToken accessToken = new AccessToken();
    accessToken.setToken("token");
    accessToken.setUser(user);
    accessToken.setExpireAt(Instant.now());
    RefreshToken refreshToken = new RefreshToken(jwtConfig, accessToken);
    refreshToken.setExpireAt(Instant.now());
    refreshTokenRepository.save(refreshToken);
    // test delete
    jwtService.deleteExpiredTokens();
    assertFalse(refreshTokenRepository.findById(refreshToken.getId()).isPresent());
    assertFalse(accessTokenRepository.findById(accessToken.getId()).isPresent());
  }
}
