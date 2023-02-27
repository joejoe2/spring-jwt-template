package com.joejoe2.demo.validation.validator;

import com.joejoe2.demo.exception.ValidationError;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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