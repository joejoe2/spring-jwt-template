package com.joejoe2.demo.validation.servivelayer;

import com.joejoe2.demo.exception.ValidationError;

import java.util.regex.Pattern;

public class UserNameValidator extends Validator<String, String>{
    private static final Pattern pattern=Pattern.compile("[a-zA-Z0-9]+");

    @Override
    public String validate(String data) throws ValidationError {
        String username = data;
        if (username==null)throw new ValidationError("username can not be null !");
        username = username.trim();

        if (username.length()==0)throw new ValidationError("username can not be empty !");
        if (username.length()>32)throw new ValidationError("the length of username is at most 32 !");
        if (!pattern.matcher(username).matches()) throw new ValidationError("username can only contain letters and numbers !");

        return username;
    }
}
