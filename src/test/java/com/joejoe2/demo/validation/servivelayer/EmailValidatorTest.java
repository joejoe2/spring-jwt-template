package com.joejoe2.demo.validation.servivelayer;

import com.joejoe2.demo.exception.ValidationError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

    @Test
    void validate() {
        EmailValidator emailValidator = new EmailValidator();

        assertThrows(ValidationError.class, ()->emailValidator.validate(null));
        assertThrows(ValidationError.class, ()->emailValidator.validate(""));
        assertThrows(ValidationError.class, ()->emailValidator.validate("   "));
        assertThrows(ValidationError.class, ()->emailValidator.validate("aaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        assertThrows(ValidationError.class, ()->emailValidator.validate("not a email"));

        assertDoesNotThrow(()->emailValidator.validate("test@email.com"));
    }
}