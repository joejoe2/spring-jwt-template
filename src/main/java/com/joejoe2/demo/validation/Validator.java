package com.joejoe2.demo.validation;

import com.joejoe2.demo.exception.ValidationError;

public abstract class Validator {
    protected boolean isValid=false;

    public abstract boolean validate() throws ValidationError;

    public boolean isValid(){
        return isValid;
    };
}
