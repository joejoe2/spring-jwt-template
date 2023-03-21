package com.joejoe2.demo.config;

import com.joejoe2.demo.interceptor.ControllerConstraintInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class InterceptorConfig implements WebMvcConfigurer {
  @Autowired ControllerConstraintInterceptor controllerConstraintInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(controllerConstraintInterceptor).addPathPatterns("/**");
  }
}
