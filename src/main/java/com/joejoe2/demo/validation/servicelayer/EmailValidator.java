package com.joejoe2.demo.validation.servicelayer;

import com.joejoe2.demo.exception.ValidationError;

import java.util.regex.Pattern;

public class EmailValidator extends Validator<String, String>{
    //ref: https://www.baeldung.com/java-email-validation-regex
    public static final String REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    private static final Pattern pattern=Pattern.compile(REGEX);
    public static final String NOT_MATCH_MSG = "invalid email format !";

    @Override
    public String validate(String data) throws ValidationError {
        String email = data;
        if (email==null)throw new ValidationError("email can not be null !");
        email=email.trim();

        if (email.length()==0)throw new ValidationError("email can not be empty !");
        if (email.length()>64)throw new ValidationError("the length of email is at most 64 !");
        if (!pattern.matcher(email).matches()) throw new ValidationError(NOT_MATCH_MSG);

        return email;
    }
}
