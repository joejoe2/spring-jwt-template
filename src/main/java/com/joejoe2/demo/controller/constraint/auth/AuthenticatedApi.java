package com.joejoe2.demo.controller.constraint.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface AuthenticatedApi {
    String rejectMessage() default "";

    int rejectStatus() default 401;
}
