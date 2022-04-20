package com.joejoe2.demo.data.auth.request;

import com.joejoe2.demo.validation.constraint.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueVerificationCodeRequest {
    @Email
    private String email;
}
