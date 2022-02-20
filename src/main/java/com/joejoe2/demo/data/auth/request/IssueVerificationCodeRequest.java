package com.joejoe2.demo.data.auth.request;

import com.joejoe2.demo.validation.constraint.Email;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class IssueVerificationCodeRequest {
    @Email
    private String email;
}
