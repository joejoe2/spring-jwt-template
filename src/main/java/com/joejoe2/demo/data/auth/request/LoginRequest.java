package com.joejoe2.demo.data.auth.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class LoginRequest {
    @NotEmpty(message = "username cannot be empty !")
    private String username;

    @NotEmpty(message = "password cannot be empty !")
    private String password;
}
