package com.joejoe2.demo.service.jwt;

import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.auth.request.AccessTokenSpec;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.AccessToken;
import com.joejoe2.demo.model.auth.RefreshToken;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.jwt.AccessTokenRepository;
import com.joejoe2.demo.repository.jwt.RefreshTokenRepository;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.service.redis.RedisService;
import com.joejoe2.demo.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {
    @Autowired
    private JwtConfig jwtConfig;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private AccessTokenRepository accessTokenRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private RedisService redisService;
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private AccessToken createAccessToken(User user) {
        AccessToken accessToken = new AccessToken(jwtConfig, user);
        accessTokenRepository.save(accessToken);
        return accessToken;
    }

    private RefreshToken createRefreshToken(AccessToken accessToken) {
        RefreshToken refreshToken = new RefreshToken(jwtConfig, accessToken);
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public TokenPair issueTokens(UserDetail userDetail) throws UserDoesNotExist {
        User user = userRepository.getByUserName(userDetail.getUsername())
                .orElseThrow(() -> new UserDoesNotExist("user is not exist !"));
        AccessToken accessToken = createAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(accessToken);

        //prevent concurrent user role/password/active change
        user.setAuthAt(Instant.now());
        userRepository.save(user);

        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TokenPair refreshTokens(String refreshPlainToken) throws InvalidTokenException, InvalidOperation {
        //parse refresh token
        try {
            JwtUtil.parseToken(jwtConfig.getPublicKey(), refreshPlainToken);
        } catch (JwtException e) {
            throw new InvalidTokenException("invalid refresh token !");
        }
        //load refresh token
        RefreshToken refreshToken = refreshTokenRepository
                .getByTokenAndExpireAtGreaterThan(refreshPlainToken, Instant.now())
                .orElseThrow(() -> new InvalidTokenException("invalid refresh token !"));
        User user = refreshToken.getUser();
        if (!user.isActive())
            throw new InvalidOperation("cannot refresh tokens for inactive user !");

        // refresh token will be cascade deleted
        revokeAccessToken(refreshToken.getAccessToken());

        //issue new tokens
        AccessToken accessToken = createAccessToken(user);
        RefreshToken newRefreshToken = createRefreshToken(accessToken);

        //prevent concurrent user role/password/active change
        user.setAuthAt(Instant.now());
        userRepository.save(user);

        return new TokenPair(accessToken, newRefreshToken);
    }

    @Override
    public UserDetail getUserDetailFromAccessToken(String token) throws InvalidTokenException {
        return JwtUtil.extractUserDetailFromAccessToken(jwtConfig.getPublicKey(), token);
    }

    @Override
    public AccessTokenSpec introspect(String token) throws InvalidTokenException {
        if (isAccessTokenInBlackList(token))
            throw new InvalidTokenException("invalid token !");

        Map<String, Object> data = JwtUtil.parseToken(jwtConfig.getPublicKey(), token);
        AccessTokenSpec spec = new AccessTokenSpec();
        for (Field field : AccessTokenSpec.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                field.set(spec, data.get(field.getName()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return spec;
    }

    @Override
    public void revokeAccessToken(String accessPlainToken) throws InvalidTokenException {
        AccessToken accessToken = accessTokenRepository
                .getByTokenAndExpireAtGreaterThan(accessPlainToken, Instant.now())
                .orElseThrow(() -> new InvalidTokenException("invalid token !"));

        accessTokenRepository.delete(accessToken); // refreshToken will be cascade deleted
        addAccessTokenToBlackList(accessToken);
    }

    @Override
    public void revokeAccessToken(AccessToken accessToken) {
        accessTokenRepository.delete(accessToken); // refreshToken will be cascade deleted
        addAccessTokenToBlackList(accessToken);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revokeAccessToken(List<AccessToken> accessTokens) {
        accessTokenRepository.deleteAll(accessTokens); // refreshToken will be cascade deleted
        accessTokens.forEach(this::addAccessTokenToBlackList);
    }

    @Override
    public void addAccessTokenToBlackList(AccessToken accessToken) {
        redisService.set("revoked_access_token:{" + accessToken.getToken() + "}", "", Duration.ofSeconds(jwtConfig.getAccessTokenLifetimeSec()));
    }

    @Override
    public boolean isAccessTokenInBlackList(String accessPlainToken) {
        return redisService.has("revoked_access_token:{" + accessPlainToken + "}");
    }

    @Transactional
    @Override
    public void deleteExpiredTokens() {
        logger.info("delete expired tokens");
        //access token will be cascade delete
        refreshTokenRepository.deleteAllByExpireAtLessThan(Instant.now());
    }
}
