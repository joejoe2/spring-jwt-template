package com.joejoe2.demo.service;

import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.AccessToken;
import com.joejoe2.demo.model.RefreshToken;
import com.joejoe2.demo.model.Role;
import com.joejoe2.demo.model.User;
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
import java.util.Map;
import java.util.UUID;

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
        accessToken.setToken(JwtUtil.generateAccessToken(jwtConfig.getKEY(), jwtConfig.getIssuer(), user, exp));
        accessToken.setUser(user);
        accessToken.setExpireAt(exp.toInstant().atZone(exp.getTimeZone().toZoneId()).toLocalDateTime());
        accessTokenRepository.save(accessToken);
        return accessToken;
    }

    private RefreshToken createRefreshToken(AccessToken accessToken) {
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, jwtConfig.getRefreshTokenLifetimeSec());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(JwtUtil.generateRefreshToken(jwtConfig.getKEY(), jwtConfig.getIssuer(), accessToken.getUser(), exp));
        refreshToken.setAccessToken(accessToken);
        refreshToken.setUser(accessToken.getUser());
        refreshToken.setExpireAt(exp.toInstant().atZone(exp.getTimeZone().toZoneId()).toLocalDateTime());
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TokenPair issueTokens(UserDetail userDetail) throws InvalidOperation {
        AccessToken accessToken = createAccessToken(userRepository.getByUserName(userDetail.getUsername()).orElseThrow(()->new InvalidOperation("user is not exist !")));
        RefreshToken refreshToken = createRefreshToken(accessToken);
        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TokenPair issueTokens(User user) {
        AccessToken accessToken = createAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(accessToken);
        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TokenPair refreshTokens(String refreshPlainToken) throws InvalidTokenException {
        //load refreshToken
        RefreshToken refreshToken = refreshTokenRepository
                .getByTokenAndExpireAtGreaterThan(refreshPlainToken, LocalDateTime.now())
                .orElseThrow(()->new InvalidTokenException("invalid token !"));
        User user = refreshToken.getUser();

        // refreshToken will be cascade deleted
        revokeAccessToken(refreshToken.getAccessToken());

        //issue new tokens
        return issueTokens(user);
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtServiceImpl.class);

    @Override
    public UserDetail getUserDetailFromAccessToken(String token) throws InvalidTokenException {
        try {
            Map<String, Object> data = JwtUtil.parseToken(jwtConfig.getKEY(), token);
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revokeAccessToken(String accessPlainToken) throws InvalidTokenException {
        AccessToken accessToken = accessTokenRepository
                .getByTokenAndExpireAtGreaterThan(accessPlainToken, LocalDateTime.now())
                .orElseThrow(()->new InvalidTokenException("invalid token !"));

        accessTokenRepository.delete(accessToken); // refreshToken will be cascade deleted

        //add to black list
        if (!addAccessTokenToBlackList(accessToken)){
            throw new InvalidTokenException("invalid token !");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void revokeAccessToken(AccessToken accessToken) throws InvalidTokenException {
        accessTokenRepository.delete(accessToken); // refreshToken will be cascade deleted
        //add to black list
        if (!addAccessTokenToBlackList(accessToken)){
            throw new InvalidTokenException("invalid token !");
        }
    }

    @Override
    public boolean addAccessTokenToBlackList(AccessToken accessToken){
        return redisService.set("access_token:"+accessToken.getToken(), "", Duration.ofSeconds(jwtConfig.getAccessTokenLifetimeSec()));
    }

    @Override
    public boolean isAccessTokenInBlackList(String accessPlainToken){
        return redisService.has("access_token:"+accessPlainToken);
    }
}
