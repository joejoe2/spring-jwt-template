package com.joejoe2.demo.controller.constraint.rate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * use token bucket algo to apply rate limit on method
 * of any controller, the key is to specify the scope of
 * rate limit(typically you should set this to api path).
 * if you are using default key="",  please keep all @RateLimit
 * having same target and key with same limit and period !!!
 * otherwise, the bucket will be refilled at different speed !!!
 */
@Target({ElementType.METHOD})
@Retention(RUNTIME)
public @interface RateLimit {
    String key() default "";

    LimitTarget target() default LimitTarget.IP;

    long limit() default 10;

    long period() default 60;
}
