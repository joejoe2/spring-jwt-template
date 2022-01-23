package com.joejoe2.demo.validation;

import com.joejoe2.demo.exception.ValidationError;

import java.util.regex.Pattern;

public class EmailValidator extends Validator{
    public String email;
    //ref: https://www.baeldung.com/java-email-validation-regex
    private Pattern pattern=Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    public EmailValidator(String email) {
        this.email = email;
    }

    @Override
    public boolean validate() throws ValidationError {
        if (email==null)throw new ValidationError("email can not be empty !");
        email=email.trim();

        if (email.length()==0)throw new ValidationError("email can not be empty !");
        if (email.length()>64)throw new ValidationError("the length of email is at most 64 !");
        if (!pattern.matcher(email).matches()) throw new ValidationError("invalid email format !");

        isValid=true;
        return isValid();
    }
}
