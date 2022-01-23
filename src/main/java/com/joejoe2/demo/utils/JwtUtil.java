package com.joejoe2.demo.utils;

import com.joejoe2.demo.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Calendar;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtUtil {
    public static String generateAccessToken(String key, String issuer, User user, Calendar exp){
        Claims claims = Jwts.claims();
        claims.put("type", "access_token");
        claims.put("id", user.getId().toString());
        claims.put("username", user.getUserName());
        claims.put("role", user.getRole().toString());
        claims.put("isActive", user.isActive());
        claims.setExpiration(exp.getTime());
        claims.setIssuer(issuer);

        Key secretKey = Keys.hmacShaKeyFor(key.getBytes());

        return Jwts.builder().setClaims(claims).signWith(secretKey).compact();
    }

    public static String generateRefreshToken(String key, String issuer, User user, Calendar exp){
        Claims claims = Jwts.claims();
        claims.put("type", "refresh_token");
        claims.put("id", user.getId().toString());
        claims.put("username", user.getUserName());
        claims.setExpiration(exp.getTime());
        claims.setIssuer(issuer);

        Key secretKey = Keys.hmacShaKeyFor(key.getBytes());

        return Jwts.builder().setClaims(claims).signWith(secretKey).compact();
    }

    public static Map<String, Object> parseToken(String key, String token) throws JwtException {
        Key secretKey = Keys.hmacShaKeyFor(key.getBytes());

        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build();

        Claims claims = parser
                .parseClaimsJws(token)
                .getBody();

        return claims.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
