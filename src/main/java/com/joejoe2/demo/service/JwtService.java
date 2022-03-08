package com.joejoe2.demo.service;

import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.auth.AccessToken;

import java.util.List;

public interface JwtService {
    TokenPair issueTokens(UserDetail userDetail) throws InvalidOperation;

    TokenPair refreshTokens(String refreshPlainToken) throws InvalidTokenException;

    UserDetail getUserDetailFromAccessToken(String token) throws InvalidTokenException;

    void revokeAccessToken(String token) throws InvalidTokenException;

    void revokeAccessToken(AccessToken accessToken);

    void revokeAccessToken(List<AccessToken> accessToken);

    void addAccessTokenToBlackList(AccessToken accessToken);

    boolean isAccessTokenInBlackList(String accessPlainToken);
}
