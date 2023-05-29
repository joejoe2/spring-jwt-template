package com.joejoe2.demo.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.data.auth.AccessTokenSpec;
import com.joejoe2.demo.data.auth.RefreshTokenSpec;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Calendar;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class JwtUtilTest {
  @Autowired JwtConfig jwtConfig;

  @Test
  void generateAccessToken() {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setUserName("test");
    user.setRole(Role.ADMIN);
    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.SECOND, 900);

    String accessToken =
        JwtUtil.generateAccessToken(
            jwtConfig.getPrivateKey(), "jti", jwtConfig.getIssuer(), user, exp);
    AccessTokenSpec data = JwtUtil.parseAccessToken(jwtConfig.getPublicKey(), accessToken);
    assertEquals("access_token", data.getType());
    assertEquals("jti", data.getJti());
    assertEquals(user.getId().toString(), data.getId());
    assertEquals(user.getUserName(), data.getUsername());
    assertEquals(user.getRole(), Role.valueOf(data.getRole()));
    assertEquals(user.isActive(), data.getIsActive());
    assertEquals(jwtConfig.getIssuer(), data.getIss());
    assertEquals((int) (exp.getTimeInMillis() / 1000), data.getExp());
  }

  @Test
  void generateRefreshToken() {
    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.SECOND, 1800);

    String accessToken =
        JwtUtil.generateRefreshToken(jwtConfig.getPrivateKey(), "jti", jwtConfig.getIssuer(), exp);
    RefreshTokenSpec data = JwtUtil.parseRefreshToken(jwtConfig.getPublicKey(), accessToken);
    assertEquals("refresh_token", data.getType());
    assertEquals("jti", data.getJti());
    assertEquals(jwtConfig.getIssuer(), data.getIss());
    assertEquals((int) (exp.getTimeInMillis() / 1000), data.getExp());
  }

  @Test
  void parseAccessToken() {
    Claims claims = Jwts.claims();
    claims.put("type", "access_token");
    claims.put("id", UUID.randomUUID().toString());
    claims.put("username", "test");
    claims.put("role", "NORMAL");
    claims.put("isActive", true);
    claims.setExpiration(Calendar.getInstance().getTime());
    claims.setIssuer("iss");
    claims.setId(UUID.randomUUID().toString());

    // token is expired
    String expiredToken =
        Jwts.builder().setClaims(claims).signWith(jwtConfig.getPrivateKey()).compact();
    assertThrows(
        JwtException.class, () -> JwtUtil.parseAccessToken(jwtConfig.getPublicKey(), expiredToken));

    // token is not expired
    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.SECOND, 900);
    claims.setExpiration(exp.getTime());
    String validToken =
        Jwts.builder().setClaims(claims).signWith(jwtConfig.getPrivateKey()).compact();
    assertDoesNotThrow(
        () -> {
          AccessTokenSpec data = JwtUtil.parseAccessToken(jwtConfig.getPublicKey(), validToken);
          assertEquals(claims.get("type"), "access_token");
          assertEquals(claims.get("id"), data.getId());
          assertEquals(claims.get("username"), data.getUsername());
          assertEquals(claims.get("role"), data.getRole());
          assertEquals(claims.get("isActive"), data.getIsActive());
          assertEquals((int) (exp.getTimeInMillis() / 1000), data.getExp());
          assertEquals(claims.getIssuer(), data.getIss());
          assertEquals(claims.getId(), data.getJti());
        });

    // invalid token
    claims.remove("id");
    String invalidToken =
        Jwts.builder().setClaims(claims).signWith(jwtConfig.getPrivateKey()).compact();
    assertThrows(
        JwtException.class, () -> JwtUtil.parseAccessToken(jwtConfig.getPublicKey(), invalidToken));
    assertThrows(
        JwtException.class,
        () -> JwtUtil.parseAccessToken(jwtConfig.getPublicKey(), "invalid_token"));
  }

  @Test
  void parseRefreshToken() {
    Claims claims = Jwts.claims();
    claims.put("type", "refresh_token");
    claims.setExpiration(Calendar.getInstance().getTime());
    claims.setIssuer("iss");
    claims.setId(UUID.randomUUID().toString());

    // token is expired
    String expiredToken =
        Jwts.builder().setClaims(claims).signWith(jwtConfig.getPrivateKey()).compact();
    assertThrows(
        JwtException.class,
        () -> JwtUtil.parseRefreshToken(jwtConfig.getPublicKey(), expiredToken));

    // token is not expired
    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.SECOND, 900);
    claims.setExpiration(exp.getTime());
    String validToken =
        Jwts.builder().setClaims(claims).signWith(jwtConfig.getPrivateKey()).compact();
    assertDoesNotThrow(
        () -> {
          RefreshTokenSpec data = JwtUtil.parseRefreshToken(jwtConfig.getPublicKey(), validToken);
          assertEquals(claims.get("type"), "refresh_token");
          assertEquals((int) (exp.getTimeInMillis() / 1000), data.getExp());
          assertEquals(claims.getIssuer(), data.getIss());
          assertEquals(claims.getId(), data.getJti());
        });

    // invalid token
    claims.remove("type");
    String invalidToken =
        Jwts.builder().setClaims(claims).signWith(jwtConfig.getPrivateKey()).compact();
    assertThrows(
        JwtException.class,
        () -> JwtUtil.parseRefreshToken(jwtConfig.getPublicKey(), invalidToken));
    assertThrows(
        JwtException.class,
        () -> JwtUtil.parseRefreshToken(jwtConfig.getPublicKey(), "invalid_token"));
  }

  @Test
  void extractUserDetailFromAccessToken() throws InvalidTokenException {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setUserName("test");
    user.setRole(Role.ADMIN);
    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.SECOND, 900);

    // test success
    String accessToken =
        JwtUtil.generateAccessToken(
            jwtConfig.getPrivateKey(), "jti", jwtConfig.getIssuer(), user, exp);
    UserDetail userDetail =
        JwtUtil.extractUserDetailFromAccessToken(jwtConfig.getPublicKey(), accessToken);
    assertEquals(user.getId().toString(), userDetail.getId());
    assertEquals(user.getUserName(), userDetail.getUsername());
    assertEquals(user.getRole(), userDetail.getRole());
    assertEquals(user.isActive(), userDetail.isActive());
    assertEquals(accessToken, userDetail.getCurrentAccessToken());
    assertEquals("jti", userDetail.getCurrentAccessTokenID());
  }
}
