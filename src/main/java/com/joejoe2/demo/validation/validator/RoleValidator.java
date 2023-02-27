package com.joejoe2.demo.validation.validator;

import com.joejoe2.demo.exception.ValidationError;
import com.joejoe2.demo.validation.constraint.Role;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RoleValidator implements ConstraintValidator<Role, String>, Validator<com.joejoe2.demo.model.auth.Role, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            com.joejoe2.demo.model.auth.Role.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public com.joejoe2.demo.model.auth.Role validate(String data) throws ValidationError {
        try {
            return com.joejoe2.demo.model.auth.Role.valueOf(data);
        } catch (IllegalArgumentException e) {
            throw new ValidationError("role " + data + " is not exist !");
        }
    }

    private static final RoleValidator instance = new RoleValidator();

    public static RoleValidator getInstance() {
        return instance;
    }

    private RoleValidator() {
    }
}
