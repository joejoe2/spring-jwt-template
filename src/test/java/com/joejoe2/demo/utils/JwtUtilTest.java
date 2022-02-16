package com.joejoe2.demo.utils;

import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private String jwtKey="asfjjfpsdajfosdofaopdspoahuigiuibnopijhvvvvvvv";
    private String issuer="issuer";

    @Test
    void generateAccessToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("test");
        user.setRole(Role.ADMIN);
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, 900);

        String accessToken = JwtUtil.generateAccessToken(jwtKey, issuer, user, exp);
        Map<String, Object> data = JwtUtil.parseToken(jwtKey, accessToken);
        assertEquals("access_token", data.get("type"));
        assertEquals(user.getId().toString(), data.get("id"));
        assertEquals(user.getUserName(), data.get("username"));
        assertEquals(user.getRole(), Role.valueOf((String) data.get("role")));
        assertEquals(user.isActive(), data.get("isActive"));
        assertEquals(issuer, data.get("iss"));
        assertEquals((int) (exp.getTimeInMillis()/1000), data.get("exp"));
    }

    @Test
    void generateRefreshToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("test");
        user.setRole(Role.ADMIN);
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, 1800);

        String accessToken = JwtUtil.generateRefreshToken(jwtKey, issuer, user, exp);
        Map<String, Object> data = JwtUtil.parseToken(jwtKey, accessToken);
        assertEquals("refresh_token", data.get("type"));
        assertEquals(user.getId().toString(), data.get("id"));
        assertEquals(user.getUserName(), data.get("username"));
        assertEquals(issuer, data.get("iss"));
        assertEquals((int) (exp.getTimeInMillis()/1000), data.get("exp"));
    }

    @Test
    void parseToken() {
        Claims claims = Jwts.claims();
        claims.put("content", 12345678);
        claims.setExpiration(Calendar.getInstance().getTime());
        claims.setIssuer(issuer);
        Key secretKey = Keys.hmacShaKeyFor(jwtKey.getBytes());
        String token = Jwts.builder().setClaims(claims).signWith(secretKey).compact();

        //token is expired
        assertThrows(JwtException.class, ()->JwtUtil.parseToken(jwtKey, token));

        //token is not expired
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, 900);
        claims.setExpiration(exp.getTime());

        //key is not match with token
        assertThrows(JwtException.class, ()->JwtUtil.parseToken("invalid_key", token));

        //invalid token
        assertThrows(JwtException.class, ()->JwtUtil.parseToken(jwtKey, "invalid_token"));
    }
}