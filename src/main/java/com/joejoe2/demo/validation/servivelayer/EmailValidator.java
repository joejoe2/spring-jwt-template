package com.joejoe2.demo.validation.servivelayer;

import com.joejoe2.demo.exception.ValidationError;

import java.util.regex.Pattern;

public class EmailValidator extends Validator<String, String>{
    //ref: https://www.baeldung.com/java-email-validation-regex
    private static final Pattern pattern=Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    @Override
    public String validate(String data) throws ValidationError {
        String email = data;
        if (email==null)throw new ValidationError("email can not be null !");
        email=email.trim();

        if (email.length()==0)throw new ValidationError("email can not be empty !");
        if (email.length()>64)throw new ValidationError("the length of email is at most 64 !");
        if (!pattern.matcher(email).matches()) throw new ValidationError("invalid email format !");

        return email;
    }
}
