package com.joejoe2.demo.validation;

import com.joejoe2.demo.exception.ValidationError;

import java.util.regex.Pattern;

public class PasswordValidator extends Validator{
    private String password;
    private Pattern pattern=Pattern.compile("[a-zA-Z0-9]+");

    public PasswordValidator(String password) {
        this.password = password;
    }

    @Override
    public boolean validate() throws ValidationError {
        if (password==null)throw new ValidationError("password can not be empty !");
        password=password.trim();

        if (password.length()==0)throw new ValidationError("password can not be empty !");
        if (password.length()>64)throw new ValidationError("the length of password is at most 32 !");
        if (!pattern.matcher(password).matches()) throw new ValidationError("password can only contain letters and numbers !");

        isValid=true;
        return isValid();
    }
}
