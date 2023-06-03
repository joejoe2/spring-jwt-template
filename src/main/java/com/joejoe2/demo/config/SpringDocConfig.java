package com.joejoe2.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(info = @Info(title = "Spring JWT Template API", version = "v0.0.1"))
@SecuritySchemes({
  @SecurityScheme(
      name = "jwt",
      scheme = "bearer",
      bearerFormat = "jwt",
      type = SecuritySchemeType.HTTP,
      in = SecuritySchemeIn.HEADER),
  @SecurityScheme(
      name = "jwt-in-cookie",
      paramName = "access_token",
      scheme = "bearer",
      bearerFormat = "jwt",
      type = SecuritySchemeType.HTTP,
      in = SecuritySchemeIn.COOKIE)
})
@Configuration
public class SpringDocConfig {}
