package com.joejoe2.demo.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Data
@Configuration
public class JwtConfig {
    @Value("${jwt.secret.privateKey}")
    private RSAPrivateKey privateKey;
    @Value("${jwt.secret.publicKey}")
    private RSAPublicKey publicKey;
    @Value("${jwt.access.token.lifetime:600}")
    private int accessTokenLifetimeSec;
    @Value("${jwt.refresh.token.lifetime:900}")
    private int refreshTokenLifetimeSec;
    @Value("${jwt.issuer:issuer}")
    private String issuer;
}
