package com.joejoe2.demo.service;

import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.auth.AccessToken;

import java.util.List;

public interface JwtService {
    /**
     * issue access and refresh token with userDetail
     * @param userDetail
     * @return generated access and refresh tokens
     * @throws InvalidOperation if user of userDetail does not exist
     */
    TokenPair issueTokens(UserDetail userDetail) throws InvalidOperation;

    /**
     * use refresh token(in plaintext) to exchange new access and refresh token, then
     * the old refresh token will be deleted and the related access token will also be
     * revoked
     * @param refreshPlainToken refresh token(in plaintext)
     * @return generated access and refresh token
     * @throws InvalidTokenException if the refresh token(in plaintext) is invalid
     */
    TokenPair refreshTokens(String refreshPlainToken) throws InvalidTokenException;

    /**
     * retrieve UserDetail from access token
     * @param token access token in plaintext
     * @return related UserDetail with the access token
     * @throws InvalidTokenException if the access token is invalid
     */
    UserDetail getUserDetailFromAccessToken(String token) throws InvalidTokenException;

    /**
     * delete access token in db then add it to the redis blacklist
     * @param token access token in plaintext
     * @throws InvalidTokenException if the access token does not exist
     */
    void revokeAccessToken(String token) throws InvalidTokenException;

    /**
     * delete access token in db then add it to the redis blacklist
     * @param accessToken access token
     */
    void revokeAccessToken(AccessToken accessToken);

    /**
     * delete access tokens in db then add them to the redis blacklist
     * @param accessTokens access tokens
     */
    void revokeAccessToken(List<AccessToken> accessTokens);

    /**
     * add access token to the redis blacklist
     * @param accessToken access token
     */
    void addAccessTokenToBlackList(AccessToken accessToken);

    /**
     * check whether the access token is in redis blacklist
     * @param accessPlainToken access token in plaintext
     * @return whether the access token is in redis blacklist
     */
    boolean isAccessTokenInBlackList(String accessPlainToken);
}
