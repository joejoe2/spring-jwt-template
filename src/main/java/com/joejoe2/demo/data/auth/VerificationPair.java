package com.joejoe2.demo.data.auth;

import com.joejoe2.demo.validation.constraint.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class VerificationPair {
    @UUID(message = "invalid verification key !")
    @NotEmpty(message = "verification key cannot be empty !")
    private String key;

    @NotEmpty(message = "verification code cannot be empty !")
    private String code;

    public VerificationPair(String key, String verificationCode) {
        this.key = key;
        this.code = verificationCode;
    }
}
