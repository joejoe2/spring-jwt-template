package com.joejoe2.demo.data.auth;

import com.joejoe2.demo.model.AccessToken;
import com.joejoe2.demo.model.RefreshToken;
import lombok.Data;

@Data
public class TokenPair {
    private AccessToken accessToken;
    private RefreshToken refreshToken;

    public TokenPair(AccessToken accessToken, RefreshToken refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
