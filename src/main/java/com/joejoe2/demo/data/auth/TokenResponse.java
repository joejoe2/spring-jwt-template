package com.joejoe2.demo.data.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TokenResponse {
    @Schema(description = "access token")
    private String access_token;
    @Schema(description = "refresh token")
    private String refresh_token;

    public TokenResponse(TokenPair tokenPair) {
        this.access_token = tokenPair.getAccessToken().getToken();
        this.refresh_token = tokenPair.getRefreshToken().getToken();
    }
}
