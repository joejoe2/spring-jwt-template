package com.joejoe2.demo.data.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class RefreshRequest {
    @NotEmpty(message = "refresh token cannot be empty !")
    @JsonProperty("token")
    private String refreshToken;
}
