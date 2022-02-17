package com.joejoe2.demo.data.auth.request;

import com.joejoe2.demo.validation.constraint.Password;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class ChangePasswordRequest {
    @NotEmpty(message = "password cannot be empty !")
    String oldPassword;

    @Size(min = 8, message = "password length is at least 8 !")
    @Size(max = 32, message = "password length is at most 32 !")
    @Password
    @NotEmpty(message = "password cannot be empty !")
    String newPassword;
}
