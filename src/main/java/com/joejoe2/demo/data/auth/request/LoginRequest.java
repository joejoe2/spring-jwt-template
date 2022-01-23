package com.joejoe2.demo.data.auth.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
