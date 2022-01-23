package com.joejoe2.demo.service;

import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.AccessToken;
import com.joejoe2.demo.model.User;

public interface JwtService {
    public TokenPair issueTokens(UserDetail userDetail) throws InvalidOperation;

    public TokenPair issueTokens(User user);

    public TokenPair refreshTokens(String refreshPlainToken) throws InvalidTokenException;

    public UserDetail getUserDetailFromAccessToken(String token) throws InvalidTokenException;

    public void revokeAccessToken(String token) throws InvalidTokenException;

    void revokeAccessToken(AccessToken accessToken) throws InvalidTokenException;

    boolean addAccessTokenToBlackList(AccessToken accessToken);

    boolean isAccessTokenInBlackList(String accessPlainToken);
}
