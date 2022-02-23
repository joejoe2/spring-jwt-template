package com.joejoe2.demo.service;

import java.time.Duration;
import java.util.Optional;

public interface RedisService {
    void set(String key, String value, Duration duration);
    Optional<String> get(String key);
    boolean has(String key);
}
