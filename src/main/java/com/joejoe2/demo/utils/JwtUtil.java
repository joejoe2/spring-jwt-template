package com.joejoe2.demo.utils;

import com.joejoe2.demo.data.auth.AccessTokenSpec;
import com.joejoe2.demo.data.auth.RefreshTokenSpec;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.lang.reflect.Field;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;

public class JwtUtil {
  public static String generateAccessToken(
      RSAPrivateKey key, String jti, String issuer, User user, Calendar exp) {
    Claims claims = Jwts.claims();
    claims.put("type", "access_token");
    claims.put("id", user.getId().toString());
    claims.put("username", user.getUserName());
    claims.put("role", user.getRole().toString());
    claims.put("isActive", user.isActive());
    claims.setExpiration(exp.getTime());
    claims.setIssuer(issuer);
    claims.setId(jti);
    return Jwts.builder().setClaims(claims).signWith(key).compact();
  }

  public static UserDetail extractUserDetailFromAccessToken(RSAPublicKey publicKey, String token)
      throws InvalidTokenException {
    try {
      AccessTokenSpec data = JwtUtil.parseAccessToken(publicKey, token);
      if (!data.getType().equals("access_token")) {
        throw new InvalidTokenException("invalid token !");
      }
      return new UserDetail(
          data.getId(),
          data.getUsername(),
          data.getIsActive(),
          Role.valueOf(data.getRole()),
          token,
          data.getJti());
    } catch (Exception ex) {
      throw new InvalidTokenException("invalid token !");
    }
  }

  public static String generateRefreshToken(
      RSAPrivateKey key, String jti, String issuer, Calendar exp) {
    Claims claims = Jwts.claims();
    claims.put("type", "refresh_token");
    claims.setExpiration(exp.getTime());
    claims.setIssuer(issuer);
    claims.setId(jti);
    return Jwts.builder().setClaims(claims).signWith(key).compact();
  }

  public static AccessTokenSpec parseAccessToken(RSAPublicKey key, String token)
      throws JwtException {
    try {
      JwtParser parser = Jwts.parserBuilder().setSigningKey(key).build();

      Claims claims = parser.parseClaimsJws(token).getBody();

      AccessTokenSpec accessTokenSpec = new AccessTokenSpec();

      for (Field field : AccessTokenSpec.class.getDeclaredFields()) {
        try {
          field.setAccessible(true);
          String k = field.getName();
          Object v = claims.get(k);
          if (v == null) throw new Exception("Missing field %s in access token !".formatted(k));
          field.set(accessTokenSpec, v);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      return accessTokenSpec;
    } catch (Exception e) {
      throw new JwtException(e.getMessage());
    }
  }

  public static RefreshTokenSpec parseRefreshToken(RSAPublicKey key, String token)
      throws JwtException {
    try {
      JwtParser parser = Jwts.parserBuilder().setSigningKey(key).build();

      Claims claims = parser.parseClaimsJws(token).getBody();

      RefreshTokenSpec refreshTokenSpec = new RefreshTokenSpec();

      for (Field field : RefreshTokenSpec.class.getDeclaredFields()) {
        try {
          field.setAccessible(true);
          String k = field.getName();
          Object v = claims.get(k);
          if (v == null) throw new Exception("Missing field %s in refresh token !".formatted(k));
          field.set(refreshTokenSpec, v);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      return refreshTokenSpec;
    } catch (Exception e) {
      throw new JwtException(e.getMessage());
    }
  }
}
