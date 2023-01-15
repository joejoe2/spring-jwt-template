package com.joejoe2.demo.validation.constraint;

import com.joejoe2.demo.validation.custom.RoleValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotEmpty;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.FIELD)
@Constraint(validatedBy = {RoleValidator.class})
@Retention(RUNTIME)
@NotEmpty(message = "role cannot be empty !")
@ReportAsSingleViolation
public @interface Role {
    String message() default "invalid role !";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
