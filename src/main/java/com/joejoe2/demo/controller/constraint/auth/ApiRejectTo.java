package com.joejoe2.demo.controller.constraint.auth;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.joejoe2.demo.model.auth.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RUNTIME)
@AuthenticatedApi
public @interface ApiRejectTo {
  Role[] roles() default {};

  String rejectMessage() default "you don't have enough permission !";

  int rejectStatus() default 403;
}
