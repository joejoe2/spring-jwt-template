package com.joejoe2.demo.service;

import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.auth.AccessToken;
import com.joejoe2.demo.model.auth.RefreshToken;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.AccessTokenRepository;
import com.joejoe2.demo.repository.RefreshTokenRepository;
import com.joejoe2.demo.repository.UserRepository;
import com.joejoe2.demo.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService{
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

    private AccessToken createAccessToken(User user) {
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, jwtConfig.getAccessTokenLifetimeSec());

        AccessToken accessToken = new AccessToken();
        accessToken.setToken(JwtUtil.generateAccessToken(jwtConfig.getPrivateKey(), accessToken.getId().toString(), jwtConfig.getIssuer(), user, exp));
        accessToken.setUser(user);
        accessToken.setExpireAt(exp.toInstant().atZone(exp.getTimeZone().toZoneId()).toLocalDateTime());
        accessTokenRepository.save(accessToken);
        return accessToken;
    }

    private RefreshToken createRefreshToken(AccessToken accessToken) {
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, jwtConfig.getRefreshTokenLifetimeSec());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(JwtUtil.generateRefreshToken(jwtConfig.getPrivateKey(), refreshToken.getId().toString(), jwtConfig.getIssuer(), exp));
        refreshToken.setAccessToken(accessToken);
        refreshToken.setUser(accessToken.getUser());
        refreshToken.setExpireAt(exp.toInstant().atZone(exp.getTimeZone().toZoneId()).toLocalDateTime());
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public TokenPair issueTokens(UserDetail userDetail) throws InvalidOperation {
        User user=userRepository.getByUserName(userDetail.getUsername()).orElseThrow(()->new InvalidOperation("user is not exist !"));
        AccessToken accessToken = createAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(accessToken);

        //prevent concurrent user role/password/active change
        user.setAuthAt(LocalDateTime.now());
        userRepository.save(user);

        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TokenPair refreshTokens(String refreshPlainToken) throws InvalidTokenException {
        //parse refresh token
        try {
            JwtUtil.parseToken(jwtConfig.getPublicKey(), refreshPlainToken);
        }catch (JwtException e){
            throw new InvalidTokenException("invalid refresh token !");
        }
        //load refresh token
        RefreshToken refreshToken = refreshTokenRepository
                .getByTokenAndExpireAtGreaterThan(refreshPlainToken, LocalDateTime.now())
                .orElseThrow(()->new InvalidTokenException("invalid refresh token !"));
        User user = refreshToken.getUser();

        // refresh token will be cascade deleted
        revokeAccessToken(refreshToken.getAccessToken());

        //issue new tokens
        AccessToken accessToken = createAccessToken(user);
        RefreshToken newRefreshToken = createRefreshToken(accessToken);

        //prevent concurrent user role/password/active change
        user.setAuthAt(LocalDateTime.now());
        userRepository.save(user);

        return new TokenPair(accessToken, newRefreshToken);
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtServiceImpl.class);

    @Override
    public UserDetail getUserDetailFromAccessToken(String token) throws InvalidTokenException {
        try {
            Map<String, Object> data = JwtUtil.parseToken(jwtConfig.getPublicKey(), token);
            String tokenType = (String) data.get("type");
            if (!tokenType.equals("access_token")){
                throw new InvalidTokenException("invalid token !");
            }
            return new UserDetail((String) data.get("id"), (String) data.get("username"),
                    (Boolean) data.get("isActive"),Role.valueOf((String) data.get("role")), token);
        }catch (JwtException | NullPointerException ex){
            throw new InvalidTokenException("invalid token !");
        }
    }

    @Override
    public void revokeAccessToken(String accessPlainToken) throws InvalidTokenException {
        AccessToken accessToken = accessTokenRepository
                .getByTokenAndExpireAtGreaterThan(accessPlainToken, LocalDateTime.now())
                .orElseThrow(()->new InvalidTokenException("invalid token !"));

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
        accessTokens.forEach((a)->addAccessTokenToBlackList(a));
    }

    @Override
    public void addAccessTokenToBlackList(AccessToken accessToken){
        redisService.set("access_token:"+accessToken.getToken(), "", Duration.ofSeconds(jwtConfig.getAccessTokenLifetimeSec()));
    }

    @Override
    public boolean isAccessTokenInBlackList(String accessPlainToken){
        return redisService.has("access_token:"+accessPlainToken);
    }
}
