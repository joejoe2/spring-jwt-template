package com.joejoe2.demo.validation.constraint;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.joejoe2.demo.validation.validator.UserNameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Constraint(validatedBy = {})
@Retention(RUNTIME)
@Size(max = 32, message = "username length is at most 32 !")
@NotEmpty(message = "username cannot be empty !")
@Pattern(regexp = UserNameValidator.REGEX, message = UserNameValidator.NOT_MATCH_MSG)
public @interface Username {
  String message() default UserNameValidator.NOT_MATCH_MSG;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
