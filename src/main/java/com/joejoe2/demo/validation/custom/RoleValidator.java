package com.joejoe2.demo.validation.custom;

import com.joejoe2.demo.validation.constraint.Role;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RoleValidator implements ConstraintValidator<Role, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            com.joejoe2.demo.model.auth.Role.valueOf(value);
            return true;
        }catch (IllegalArgumentException e){
            return false;
        }
    }
}
