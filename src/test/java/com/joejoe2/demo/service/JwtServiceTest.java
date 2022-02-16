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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {
    @Autowired
    JwtService jwtService;
    @Autowired
    private JwtConfig jwtConfig;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private AccessTokenRepository accessTokenRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @Transactional
    void issueTokens() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword("pa55ward");
        user.setRole(Role.NORMAL);
        //test not exist user
        assertThrows(InvalidOperation.class, ()->jwtService.issueTokens(new UserDetail(user)));
        //test exist user
        userRepository.save(user);
        assertDoesNotThrow(()->{
            TokenPair tokenPair = jwtService.issueTokens(new UserDetail(user));
            AccessToken accessToken = tokenPair.getAccessToken();
            RefreshToken refreshToken = tokenPair.getRefreshToken();
            assertEquals(accessToken, accessTokenRepository.getById(accessToken.getId()));
            assertEquals(refreshToken, refreshTokenRepository.getById(refreshToken.getId()));
            assertEquals(accessToken.getUser(), user);
            assertEquals(refreshToken.getUser(), user);
            assertEquals(refreshToken.getAccessToken(), accessToken);
        });
    }

    @Test
    void refreshTokens() {

    }

    @Test
    void getUserDetailFromAccessToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword("pa55ward");
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, 900);
        String token = JwtUtil.generateAccessToken(jwtConfig.getKEY(), "iss", user, exp);
        assertDoesNotThrow(()->{
            UserDetail userDetail = jwtService.getUserDetailFromAccessToken(token);
            assertEquals(user.getId().toString(), userDetail.getId());
            assertEquals(user.getUserName(), userDetail.getUsername());
            assertEquals(user.getUserName(), userDetail.getUsername());
            assertEquals(user.isActive(), userDetail.isActive());
            assertEquals(user.getRole(), userDetail.getRole());
        });
    }

    @Test
    @Transactional
    void revokeAccessToken() {
        //test invalid token
        assertThrows(InvalidTokenException.class, ()->jwtService.revokeAccessToken("invalid_token"));
        //test valid token
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUserName("test");
        user.setEmail("test@email.com");
        user.setPassword("pa55ward");
        user.setRole(Role.NORMAL);
        userRepository.save(user);
        AccessToken accessToken = new AccessToken();
        accessToken.setToken("test_token");
        accessToken.setUser(user);
        accessToken.setExpireAt(LocalDateTime.now().plusMinutes(15));
        accessTokenRepository.save(accessToken);
        assertDoesNotThrow(()->{
            jwtService.revokeAccessToken(accessToken.getToken());
            assertTrue(jwtService.isAccessTokenInBlackList(accessToken.getToken()));
        });
    }

    @Test
    @Transactional
    void testRevokeAccessToken() {
        //same as above
    }

    @Test
    void addAccessTokenToBlackList() {
        //skip this
    }

    @Test
    void isAccessTokenInBlackList() {
        //skip this
    }
}