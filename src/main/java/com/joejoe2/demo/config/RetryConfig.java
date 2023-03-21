package com.joejoe2.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.retry.annotation.RetryConfiguration;

@Configuration
public class RetryConfig extends RetryConfiguration {
  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
