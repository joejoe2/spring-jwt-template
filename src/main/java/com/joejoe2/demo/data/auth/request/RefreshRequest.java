package com.joejoe2.demo.data.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
    @NotEmpty(message = "refresh token cannot be empty !")
    @JsonProperty("token")
    private String refreshToken;
}
