package com.joejoe2.demo.validation.validator;

import com.joejoe2.demo.exception.ValidationError;

import java.util.regex.Pattern;

public class UserNameValidator implements Validator<String, String> {
    public static final String REGEX = "[a-zA-Z0-9]+";
    public static final String NOT_MATCH_MSG = "username can only contain a-z, A-Z, and 0-9 !";

    private static final Pattern pattern = Pattern.compile(REGEX);

    @Override
    public String validate(String data) throws ValidationError {
        String username = data;
        if (username == null) throw new ValidationError("username can not be null !");
        username = username.trim();

        if (username.length() == 0) throw new ValidationError("username can not be empty !");
        if (username.length() > 32) throw new ValidationError("the length of username is at most 32 !");
        if (!pattern.matcher(username).matches()) throw new ValidationError(NOT_MATCH_MSG);

        return username;
    }

    private static final UserNameValidator instance = new UserNameValidator();

    public static UserNameValidator getInstance() {
        return instance;
    }

    private UserNameValidator() {
    }
}
