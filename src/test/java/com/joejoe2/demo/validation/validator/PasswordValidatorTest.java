package com.joejoe2.demo.validation.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.joejoe2.demo.exception.ValidationError;
import org.junit.jupiter.api.Test;

class PasswordValidatorTest {

  @Test
  void validate() {
    PasswordValidator passwordValidator = PasswordValidator.getInstance();

    assertThrows(ValidationError.class, () -> passwordValidator.validate(null));
    assertThrows(ValidationError.class, () -> passwordValidator.validate(""));
    assertThrows(ValidationError.class, () -> passwordValidator.validate("    "));
    assertThrows(ValidationError.class, () -> passwordValidator.validate("1234567"));
    assertThrows(
        ValidationError.class,
        () -> passwordValidator.validate("12345671234567123456712345671234567"));
    assertThrows(ValidationError.class, () -> passwordValidator.validate("********"));

    assertDoesNotThrow(() -> passwordValidator.validate("12345678"));
  }
}
