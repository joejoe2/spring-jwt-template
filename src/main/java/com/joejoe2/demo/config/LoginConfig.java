package com.joejoe2.demo.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class LoginConfig {
    @Value("${login.maxAttempts:5}")
    private int maxAttempts;

    @Value("${login.attempts.coolTime:900}")
    private int coolTime;
}
