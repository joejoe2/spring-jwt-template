package com.joejoe2.demo.data.auth.request;

import com.joejoe2.demo.validation.constraint.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgetPasswordRequest {
    @Email
    private String email;
}
