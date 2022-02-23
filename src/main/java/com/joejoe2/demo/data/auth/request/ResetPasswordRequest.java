package com.joejoe2.demo.data.auth.request;

import com.joejoe2.demo.validation.constraint.Password;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ResetPasswordRequest {
    @NotEmpty(message = "token can not be empty !")
    String token; // for verify ResetPasswordRequest

    @Password
    String newPassword;
}
