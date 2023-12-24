package com.joejoe2.demo;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;

public class TestContext implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

  static {
    GenericContainer redis = new GenericContainer("redis:6.2.7-alpine").withExposedPorts(6379);
    redis.getPortBindings().add("6370:6379");
    redis.start();

    GenericContainer postgres =
        new GenericContainer("postgres:15.1")
            .withExposedPorts(5432)
            .withEnv("POSTGRES_PASSWORD", "pa55ward")
            .withEnv("POSTGRES_DB", "spring-test");
    postgres.getPortBindings().add("5430:5432");
    postgres.start();
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    // Your "before all tests" startup logic goes here
  }

  @Override
  public void close() {
    // Your "after all tests" logic goes here
  }
}
