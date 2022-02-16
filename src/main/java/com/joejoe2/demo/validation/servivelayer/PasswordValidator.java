package com.joejoe2.demo.validation.servivelayer;

import com.joejoe2.demo.exception.ValidationError;

import java.util.regex.Pattern;

public class PasswordValidator extends Validator<String, String>{
    private static final Pattern pattern=Pattern.compile("[a-zA-Z0-9]+");

    @Override
    public String validate(String data) throws ValidationError {
        String password = data;
        if (password==null)throw new ValidationError("password can not be null !");
        password=password.trim();

        if (password.length()==0)throw new ValidationError("password can not be empty !");
        if (password.length()<8)throw new ValidationError("the length of password is at least 8 !");
        if (password.length()>32)throw new ValidationError("the length of password is at most 32 !");
        if (!pattern.matcher(password).matches()) throw new ValidationError("password can only contain letters and numbers !");

        return password;
    }
}
