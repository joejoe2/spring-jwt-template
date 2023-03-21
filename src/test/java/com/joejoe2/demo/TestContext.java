package com.joejoe2.demo;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;

public class TestContext implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

  private static boolean started = false;

  static {
    GenericContainer redis = new GenericContainer("redis:6.2.7-alpine").withExposedPorts(6379);
    redis.getPortBindings().add("6370:6379");
    redis.start();

    System.setProperty("spring.redis.host", redis.getContainerIpAddress());
    System.setProperty("spring.redis.port", redis.getFirstMappedPort() + "");

    GenericContainer postgres =
        new GenericContainer("postgres:15.1")
            .withExposedPorts(5432)
            .withEnv("POSTGRES_PASSWORD", "pa55ward")
            .withEnv("POSTGRES_DB", "spring-test");
    postgres.getPortBindings().add("5430:5432");
    postgres.start();
    System.setProperty(
        "spring.datasource.url",
        "jdbc:postgresql://localhost:" + postgres.getFirstMappedPort() + "/spring-test");
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!started) {
      started = true;
      // Your "before all tests" startup logic goes here
    }
  }

  @Override
  public void close() {
    // Your "after all tests" logic goes here
  }
}
