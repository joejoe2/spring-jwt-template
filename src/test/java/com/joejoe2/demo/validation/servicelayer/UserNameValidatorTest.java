package com.joejoe2.demo.validation.servicelayer;

import com.joejoe2.demo.exception.ValidationError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class UserNameValidatorTest {

    @Test
    void validate() {
        UserNameValidator userNameValidator = new UserNameValidator();

        assertThrows(ValidationError.class, ()->userNameValidator.validate(null));
        assertThrows(ValidationError.class, ()->userNameValidator.validate(""));
        assertThrows(ValidationError.class, ()->userNameValidator.validate("    "));
        assertThrows(ValidationError.class, ()->userNameValidator.validate("aaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        assertThrows(ValidationError.class, ()->userNameValidator.validate("***/-*-"));

        assertDoesNotThrow(()->userNameValidator.validate("test"));
    }
}