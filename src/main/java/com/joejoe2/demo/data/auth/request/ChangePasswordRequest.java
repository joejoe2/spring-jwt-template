package com.joejoe2.demo.data.auth.request;

import com.joejoe2.demo.validation.constraint.Password;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class ChangePasswordRequest {
    @NotEmpty(message = "password cannot be empty !")
    String oldPassword;

    @Password
    String newPassword;
}
