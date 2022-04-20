package com.joejoe2.demo.data.auth.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotEmpty(message = "username cannot be empty !")
    private String username;

    @NotEmpty(message = "password cannot be empty !")
    private String password;
}
