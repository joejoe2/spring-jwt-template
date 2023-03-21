package com.joejoe2.demo.validation.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.joejoe2.demo.exception.ValidationError;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UUIDValidatorTest {

  @Test
  void validate() {
    UUIDValidator uuidValidator = UUIDValidator.getInstance();

    assertThrows(ValidationError.class, () -> uuidValidator.validate(null));
    assertThrows(ValidationError.class, () -> uuidValidator.validate(""));
    assertThrows(ValidationError.class, () -> uuidValidator.validate("    "));

    assertDoesNotThrow(() -> uuidValidator.validate(UUID.randomUUID().toString()));
  }
}
