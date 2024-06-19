package com.joejoe2.demo.service.redis;

import static org.junit.jupiter.api.Assertions.*;

import com.joejoe2.demo.TestContext;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
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

  @Test
  void testOptimisticLock() throws Exception {
    String key = "testOptimisticLock";
    boolean[] lock = {false};
    Thread thread1 =
        new Thread(
            () ->
                redisTemplate.execute(
                    new SessionCallback<>() {
                      @Override
                      public Object execute(RedisOperations outerOperations)
                          throws DataAccessException {
                        outerOperations.watch(key);
                        outerOperations.multi();
                        try {
                          Thread.sleep(10000);
                        } catch (InterruptedException e) {
                          throw new RuntimeException(e);
                        }
                        outerOperations.opsForValue().set(key, "xxx");
                        lock[0] = outerOperations.exec().isEmpty();
                        return null;
                      }
                    }));

    Thread thread2 =
        new Thread(
            () ->
                redisTemplate.execute(
                    new SessionCallback<>() {
                      @Override
                      public Object execute(RedisOperations outerOperations)
                          throws DataAccessException {
                        try {
                          Thread.sleep(3000);
                        } catch (InterruptedException e) {
                          throw new RuntimeException(e);
                        }
                        outerOperations.watch(key);
                        outerOperations.multi();
                        outerOperations.opsForValue().set(key, "xxx");
                        outerOperations.exec();
                        return null;
                      }
                    }));
    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();

    assertTrue(lock[0]);
  }
}
