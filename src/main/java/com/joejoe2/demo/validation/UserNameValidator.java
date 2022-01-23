package com.joejoe2.demo.validation;

import com.joejoe2.demo.exception.ValidationError;

import java.util.regex.Pattern;

public class UserNameValidator extends Validator{
    public String username;
    private Pattern pattern=Pattern.compile("[a-zA-Z0-9]+");

    public UserNameValidator(String username) {
        this.username = username;
    }

    @Override
    public boolean validate() throws ValidationError {
        if (username==null)throw new ValidationError("username can not be empty !");
        username = username.trim();

        if (username.length()==0)throw new ValidationError("username can not be empty !");
        if (username.length()>32)throw new ValidationError("the length of username is at most 32 !");
        if (!pattern.matcher(username).matches()) throw new ValidationError("password can only contain letters and numbers !");

        isValid=true;
        return isValid();
    }
}
