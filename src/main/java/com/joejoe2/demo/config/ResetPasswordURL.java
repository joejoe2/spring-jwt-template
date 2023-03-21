package com.joejoe2.demo.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ResetPasswordURL {
  @Value("${reset.password.url}")
  String UrlPrefix;
}
