package com.joejoe2.demo.validation.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.joejoe2.demo.exception.ValidationError;
import org.junit.jupiter.api.Test;

class UserNameValidatorTest {

  @Test
  void validate() {
    UserNameValidator userNameValidator = UserNameValidator.getInstance();

    assertThrows(ValidationError.class, () -> userNameValidator.validate(null));
    assertThrows(ValidationError.class, () -> userNameValidator.validate(""));
    assertThrows(ValidationError.class, () -> userNameValidator.validate("    "));
    assertThrows(
        ValidationError.class,
        () ->
            userNameValidator.validate(
                "aaaaaaaa"
                    + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    assertThrows(ValidationError.class, () -> userNameValidator.validate("***/-*-"));

    assertDoesNotThrow(() -> userNameValidator.validate("test"));
  }
}
