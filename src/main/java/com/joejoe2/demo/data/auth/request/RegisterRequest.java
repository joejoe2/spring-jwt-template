package com.joejoe2.demo.data.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.validation.constraint.Email;
import com.joejoe2.demo.validation.constraint.Password;
import com.joejoe2.demo.validation.constraint.Username;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;

@Data
public class RegisterRequest {
    @Username
    private String username;

    @Email
    private String email;

    @Password
    private String password;

    @Valid
    @NotNull(message = "verification cannot be empty !")
    @JsonProperty("verification")
    private VerificationPair verification;
}

