package com.joejoe2.demo.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class JwtConfig {
    @Value("${jwt.secret.key:asfjjfpsdajfosdofaopdspoahuigiuibnopijhvvvvvvv}")
    private String KEY;
    @Value("${jwt.access.token.lifetime:600}")
    private int accessTokenLifetimeSec;
    @Value("${jwt.refresh.token.lifetime:900}")
    private int refreshTokenLifetimeSec;
    @Value("${jwt.issuer:issuer}")
    private String issuer;
}
