package com.joejoe2.demo.utils;

import com.joejoe2.demo.model.auth.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtUtil {
    public static String generateAccessToken(RSAPrivateKey key, String jti, String issuer, User user, Calendar exp){
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

    public static String generateRefreshToken(RSAPrivateKey key, String jti, String issuer, Calendar exp){
        Claims claims = Jwts.claims();
        claims.put("type", "refresh_token");
        claims.setExpiration(exp.getTime());
        claims.setIssuer(issuer);
        claims.setId(jti);

        return Jwts.builder().setClaims(claims).signWith(key).compact();
    }

    public static Map<String, Object> parseToken(RSAPublicKey key, String token) throws JwtException {
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .build();

        Claims claims = parser
                .parseClaimsJws(token)
                .getBody();

        return claims.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
