package com.joejoe2.demo.data.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class VerificationKey {
    @Schema(description = "used along with verification code to pass the verification")
    String key;

    public VerificationKey(String key) {
        this.key = key;
    }
}
