package com.joejoe2.demo.utils;

import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class JwtUtilTest {
    @Autowired
    JwtConfig jwtConfig;

    @Test
    void generateAccessToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("test");
        user.setRole(Role.ADMIN);
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, 900);

        String accessToken = JwtUtil.generateAccessToken(jwtConfig.getPrivateKey(), "jti", jwtConfig.getIssuer(), user, exp);
        Map<String, Object> data = JwtUtil.parseToken(jwtConfig.getPublicKey(), accessToken);
        assertEquals("access_token", data.get("type"));
        assertEquals(user.getId().toString(), data.get("id"));
        assertEquals(user.getUserName(), data.get("username"));
        assertEquals(user.getRole(), Role.valueOf((String) data.get("role")));
        assertEquals(user.isActive(), data.get("isActive"));
        assertEquals(jwtConfig.getIssuer(), data.get("iss"));
        assertEquals((int) (exp.getTimeInMillis() / 1000), data.get("exp"));
    }

    @Test
    void generateRefreshToken() {
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, 1800);

        String accessToken = JwtUtil.generateRefreshToken(jwtConfig.getPrivateKey(), "jti", jwtConfig.getIssuer(), exp);
        Map<String, Object> data = JwtUtil.parseToken(jwtConfig.getPublicKey(), accessToken);
        assertEquals("refresh_token", data.get("type"));
        assertEquals(jwtConfig.getIssuer(), data.get("iss"));
        assertEquals((int) (exp.getTimeInMillis() / 1000), data.get("exp"));
    }

    @Test
    void parseToken() {
        Claims claims = Jwts.claims();
        claims.put("content", 12345678);
        claims.setExpiration(Calendar.getInstance().getTime());
        claims.setIssuer(jwtConfig.getIssuer());
        String token = Jwts.builder().setClaims(claims).signWith(jwtConfig.getPrivateKey()).compact();

        //token is expired
        String finalToken = token;
        assertThrows(JwtException.class, () -> JwtUtil.parseToken(jwtConfig.getPublicKey(), finalToken));

        //token is not expired
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, 900);
        claims.setExpiration(exp.getTime());
        token = Jwts.builder().setClaims(claims).signWith(jwtConfig.getPrivateKey()).compact();
        String finalToken1 = token;
        assertDoesNotThrow(() -> JwtUtil.parseToken(jwtConfig.getPublicKey(), finalToken1));

        //invalid token
        assertThrows(JwtException.class, () -> JwtUtil.parseToken(jwtConfig.getPublicKey(), "invalid_token"));
    }

    @Test
    void extractUserDetailFromAccessToken() throws InvalidTokenException {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("test");
        user.setRole(Role.ADMIN);
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, 900);

        //test success
        String accessToken = JwtUtil.generateAccessToken(jwtConfig.getPrivateKey(), "jti", jwtConfig.getIssuer(), user, exp);
        UserDetail userDetail = JwtUtil.extractUserDetailFromAccessToken(jwtConfig.getPublicKey(), accessToken);
        assertEquals(user.getId().toString(), userDetail.getId());
        assertEquals(user.getUserName(), userDetail.getUsername());
        assertEquals(user.getRole(), userDetail.getRole());
        assertEquals(user.isActive(), userDetail.isActive());
    }
}