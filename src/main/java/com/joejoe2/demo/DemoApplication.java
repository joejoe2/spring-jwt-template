package com.joejoe2.demo;

import jakarta.annotation.PostConstruct;
import java.util.Locale;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DemoApplication {
  public static void main(String[] args) {
    Locale.setDefault(Locale.ENGLISH);
    SpringApplication.run(DemoApplication.class, args);
  }

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }
}
