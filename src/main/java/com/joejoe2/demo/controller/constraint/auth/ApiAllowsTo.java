package com.joejoe2.demo.controller.constraint.auth;

import com.joejoe2.demo.model.auth.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.METHOD})
@Retention(RUNTIME)
@AuthenticatedApi
public @interface ApiAllowsTo {
    Role[] roles() default {};

    String rejectMessage() default "you don't have enough permission !";

    int rejectStatus() default 403;
}
