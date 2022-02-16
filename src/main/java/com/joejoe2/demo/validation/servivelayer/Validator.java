package com.joejoe2.demo.validation.servivelayer;

import com.joejoe2.demo.exception.ValidationError;

public abstract class Validator<I, O> {
    public abstract O validate(I data) throws ValidationError;
}
