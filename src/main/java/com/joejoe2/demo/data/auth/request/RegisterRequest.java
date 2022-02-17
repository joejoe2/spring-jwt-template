package com.joejoe2.demo.data.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.validation.constraint.Password;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;

@Data
public class RegisterRequest {
    @Size(max = 32, message = "username length is at most 32 !")
    @NotEmpty(message = "username cannot be empty !")
    private String username;

    @Email(message = "must be a well-formed email address !")
    @Size(max = 64, message = "email length is at most 64 !")
    @NotEmpty(message = "email cannot be empty !")
    private String email;

    @Size(min = 8, message = "password length is at least 8 !")
    @Size(max = 32, message = "password length is at most 32 !")
    @Password
    @NotEmpty(message = "password cannot be empty !")
    private String password;

    @Valid
    @NotNull(message = "verification cannot be empty !")
    @JsonProperty("verification")
    private VerificationPair verification;
}

