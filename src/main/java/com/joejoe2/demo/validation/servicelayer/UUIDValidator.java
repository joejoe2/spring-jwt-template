package com.joejoe2.demo.validation.servicelayer;

import com.joejoe2.demo.exception.ValidationError;

import java.util.UUID;

public class UUIDValidator extends Validator<String, UUID>{
    @Override
    public UUID validate(String data) throws ValidationError {
        if (data==null)throw new ValidationError("uuid can not be null !");

        try {
            return UUID.fromString(data);
        }catch (Exception e){
            throw new ValidationError(e.getMessage());
        }
    }
}
