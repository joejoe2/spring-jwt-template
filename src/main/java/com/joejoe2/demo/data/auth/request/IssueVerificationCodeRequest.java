package com.joejoe2.demo.data.auth.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class IssueVerificationCodeRequest {
    @Email(message = "must be a well-formed email address !")
    @Size(max = 64, message = "email length is at most 64 !")
    @NotEmpty(message = "email cannot be empty !")
    private String email;
}
