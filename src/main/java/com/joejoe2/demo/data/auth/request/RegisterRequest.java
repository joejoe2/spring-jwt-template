package com.joejoe2.demo.data.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.validation.constraint.Email;
import com.joejoe2.demo.validation.constraint.Password;
import com.joejoe2.demo.validation.constraint.Username;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @Schema(description = "name of the user")
    @Username
    private String username;

    @Schema(description = "email of the user")
    @Email
    private String email;

    @Schema(description = "password of the user")
    @Password
    private String password;

    @Schema(description = "verification for the Registration")
    @Valid
    @NotNull(message = "verification cannot be empty !")
    @JsonProperty("verification")
    private VerificationPair verification;
}

