package com.joejoe2.demo.validation.constraint;

import com.joejoe2.demo.validation.servicelayer.PasswordValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.FIELD)
@Constraint(validatedBy={})
@Retention(RUNTIME)
@Pattern(regexp=PasswordValidator.REGEX)
@ReportAsSingleViolation
public @interface Password {
    String message() default PasswordValidator.NOT_MATCH_MSG;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
