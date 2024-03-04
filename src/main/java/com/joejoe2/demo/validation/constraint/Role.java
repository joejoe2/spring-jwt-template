package com.joejoe2.demo.validation.constraint;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.joejoe2.demo.validation.validator.RoleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotEmpty;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
