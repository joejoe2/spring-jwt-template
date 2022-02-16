package com.joejoe2.demo.validation.servivelayer;

import com.joejoe2.demo.exception.ValidationError;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    @Test
    void validate() {
        PasswordValidator passwordValidator = new PasswordValidator();

        assertThrows(ValidationError.class, ()->passwordValidator.validate(null));
        assertThrows(ValidationError.class, ()->passwordValidator.validate(""));
        assertThrows(ValidationError.class, ()->passwordValidator.validate("    "));
        assertThrows(ValidationError.class, ()->passwordValidator.validate("1234567"));
        assertThrows(ValidationError.class, ()->passwordValidator.validate("12345671234567123456712345671234567"));
        assertThrows(ValidationError.class, ()->passwordValidator.validate("********"));

        assertDoesNotThrow(()->passwordValidator.validate("12345678"));
    }
}