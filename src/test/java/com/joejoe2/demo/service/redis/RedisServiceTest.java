package com.joejoe2.demo.service.redis;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.joejoe2.demo.TestContext;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class RedisServiceTest {
  @Autowired RedisService redisService;
  @Autowired private StringRedisTemplate redisTemplate;

  @Test
  void set() {
    redisService.set("key1", "test", Duration.ofSeconds(30));
    assertTrue(redisTemplate.hasKey("key1"));
    assert redisTemplate.getExpire("key1", TimeUnit.SECONDS) < 30;
  }

  @Test
  void get() {
    redisTemplate.opsForValue().set("key2", "test", Duration.ofSeconds(30));
    redisService.get("key2").get().equals("test");
  }

  @Test
  void has() {
    redisTemplate.opsForValue().set("key3", "test", Duration.ofSeconds(30));
    assert redisService.has("key3");
  }
}
