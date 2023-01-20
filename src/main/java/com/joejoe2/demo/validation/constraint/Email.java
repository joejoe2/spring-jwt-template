package com.joejoe2.demo.validation.constraint;

import com.joejoe2.demo.validation.validator.EmailValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.FIELD)
@Constraint(validatedBy = {})
@Retention(RUNTIME)
@Size(max = 64, message = "email length is at most 64 !")
@NotEmpty(message = "email cannot be empty !")
@Pattern(regexp = EmailValidator.REGEX, message = EmailValidator.NOT_MATCH_MSG)
public @interface Email {
    String message() default EmailValidator.NOT_MATCH_MSG;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
