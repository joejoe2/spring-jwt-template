package com.joejoe2.demo.data.auth.request;

import com.joejoe2.demo.validation.constraint.Email;
import lombok.Data;

@Data
public class ForgetPasswordRequest {
    @Email
    private String email;
}
