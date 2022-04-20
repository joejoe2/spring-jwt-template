package com.joejoe2.demo.data.auth.request;

import com.joejoe2.demo.validation.constraint.Password;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotEmpty(message = "token can not be empty !")
    String token; // for verify ResetPasswordRequest

    @Password
    String newPassword;
}
