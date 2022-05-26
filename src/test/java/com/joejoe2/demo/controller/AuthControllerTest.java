package com.joejoe2.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.auth.VerificationPair;
import com.joejoe2.demo.data.auth.request.*;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.auth.*;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.repository.verification.VerifyTokenRepository;
import com.joejoe2.demo.service.jwt.JwtService;
import com.joejoe2.demo.service.verification.VerificationService;
import com.joejoe2.demo.utils.AuthUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import redis.embedded.RedisServer;

import javax.servlet.http.Cookie;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockBean
    JwtService jwtService;

    @MockBean
    VerificationService verificationService;

    @Autowired
    VerifyTokenRepository verifyTokenRepository;

    @Autowired
    MockMvc mockMvc;

    User user;

    @BeforeEach
    void createUser(){
        user=new User();
        user.setUserName("testUser");
        user.setRole(Role.NORMAL);
        user.setEmail("testUser@email.com");
        user.setPassword(passwordEncoder.encode("pa55ward"));
        userRepository.save(user);
    }

    @AfterEach
    void deleteUser(){
        userRepository.delete(user);
    }

    private static RedisServer redisServer;

    @BeforeAll
    static void beforeAll() {
        redisServer=RedisServer.builder().port(6370).setting("maxmemory 128M").build();
        redisServer.start();
    }

    @AfterAll
    static void afterAll() {
        redisServer.stop();
    }

    ObjectMapper objectMapper=new ObjectMapper();

    @Test
    @Transactional
    void login() throws Exception{
        //test success
        LoginRequest request=LoginRequest.builder().username(user.getUserName()).password("pa55ward").build();
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(()->AuthUtil.authenticate(authenticationManager, user.getUserName(), "pa55ward"))
                .thenReturn(new UserDetail(user));
        Mockito.doReturn(new TokenPair(new AccessToken(), new RefreshToken())).when(jwtService).issueTokens(new UserDetail(user));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").hasJsonPath())
                .andExpect(jsonPath("$.refresh_token").hasJsonPath())
                .andExpect(status().isOk());
        mockedStatic.close();
    }

    @Test
    void loginWithBadRequest() throws Exception{
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        //test 400
        //0. validation
        LoginRequest badRequest=LoginRequest.builder().username("").password("").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(status().isBadRequest());
        //1. AuthenticationException
        badRequest=LoginRequest.builder().username(user.getUserName()).password("pa55ward").build();
        mockedStatic.when(()->AuthUtil.authenticate(authenticationManager, user.getUserName(), "pa55ward"))
                .thenThrow(new AuthenticationException("") {});
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
        mockedStatic.close();
    }

    @Test
    @Transactional
    void webLogin() throws Exception{
        //test success
        LoginRequest request=LoginRequest.builder().username(user.getUserName()).password("pa55ward").build();
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(()->AuthUtil.authenticate(authenticationManager, user.getUserName(), "pa55ward"))
                .thenReturn(new UserDetail(user));
        Mockito.doReturn(new TokenPair(new AccessToken(), new RefreshToken())).when(jwtService).issueTokens(new UserDetail(user));
        Cookie cookie=mockMvc.perform(MockMvcRequestBuilders.post("/web/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").hasJsonPath())
                .andExpect(status().isOk()).andReturn().getResponse().getCookie("refresh_token");
        assertNotNull(cookie);
        assertTrue(cookie.isHttpOnly());
        mockedStatic.close();
    }

    @Test
    void webLoginWithBadRequest() throws Exception{
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        //test 400
        //0. validation
        LoginRequest badRequest=LoginRequest.builder().username("").password("").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/web/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(status().isBadRequest());
        //1. AuthenticationException
        badRequest=LoginRequest.builder().username(user.getUserName()).password("pa55ward").build();
        mockedStatic.when(()->AuthUtil.authenticate(authenticationManager, user.getUserName(), "pa55ward"))
                .thenThrow(new AuthenticationException("") {});
        mockMvc.perform(MockMvcRequestBuilders.post("/web/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
        mockedStatic.close();
    }

    @Test
    @Transactional
    void refresh() throws Exception{
        //test success
        RefreshRequest request=RefreshRequest.builder().refresh_token("refresh_token").build();
        Mockito.doReturn(new TokenPair(new AccessToken(), new RefreshToken())).when(jwtService).refreshTokens("refresh_token");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").hasJsonPath())
                .andExpect(jsonPath("$.refresh_token").hasJsonPath())
                .andExpect(status().isOk());
        //test 400
        //0. validation
        RefreshRequest badRequest=RefreshRequest.builder().refresh_token("").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.refresh_token").exists())
                .andExpect(status().isBadRequest());
        //1. InvalidTokenException
        badRequest=RefreshRequest.builder().refresh_token("invalid_token").build();
        Mockito.doThrow(new InvalidTokenException("")).when(jwtService).refreshTokens("invalid_token");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshWithBadRequest() throws Exception{
        //test 400
        //0. validation
        RefreshRequest badRequest=RefreshRequest.builder().refresh_token("").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.refresh_token").exists())
                .andExpect(status().isBadRequest());
        //1. InvalidTokenException
        badRequest=RefreshRequest.builder().refresh_token("invalid_token").build();
        Mockito.doThrow(new InvalidTokenException("")).when(jwtService).refreshTokens("invalid_token");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void webRefresh() throws Exception{
        //test success
        Mockito.doReturn(new TokenPair(new AccessToken(), new RefreshToken())).when(jwtService).refreshTokens("refresh_token");
        Cookie cookie=mockMvc.perform(MockMvcRequestBuilders.post("/web/api/auth/refresh")
                .cookie(new Cookie("refresh_token", "refresh_token")))
                .andExpect(jsonPath("$.access_token").hasJsonPath())
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("refresh_token");
        assertNotNull(cookie);
        //test 400
        //1. InvalidTokenException
        Mockito.doThrow(new InvalidTokenException("")).when(jwtService).refreshTokens("invalid_token");
        mockMvc.perform(MockMvcRequestBuilders.post("/web/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", "invalid_token")))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
    }

    @Test
    void webRefreshWithBadRequest() throws Exception{
        //test 400
        //1. InvalidTokenException
        Mockito.doThrow(new InvalidTokenException("")).when(jwtService).refreshTokens("invalid_token");
        mockMvc.perform(MockMvcRequestBuilders.post("/web/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", "invalid_token")))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout() throws Exception{
        //mock login
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
        mockedStatic.when(AuthUtil::currentUserDetail)
                .thenReturn(new UserDetail(user.getId().toString(),
                        user.getUserName(),
                        user.isActive(),
                        user.getRole(),
                        "access_token"));
        Mockito.doNothing().when(jwtService).revokeAccessToken("access_token");
        //test success
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout"))
                .andExpect(status().isOk());
        //clear login
        mockedStatic.close();
    }

    @Test
    void logoutWithInvalidToken() throws Exception{
        //mock login
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
        //test 401
        //1. InvalidTokenException
        mockedStatic.when(AuthUtil::currentUserDetail)
                .thenReturn(new UserDetail(user.getId().toString(),
                        user.getUserName(),
                        user.isActive(),
                        user.getRole(),
                        "invalid_token"));
        Mockito.doThrow(new InvalidTokenException("")).when(jwtService).revokeAccessToken("invalid_token");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
        //clear login
        mockedStatic.close();
    }

    @Test
    void register() {
    }

    @Test
    void issueVerificationCode() throws Exception{
        //test success
        IssueVerificationCodeRequest request =
                IssueVerificationCodeRequest.builder().email(user.getEmail()).build();
        String key = UUID.randomUUID().toString();
        Mockito.when(verificationService.issueVerificationCode(Mockito.any()))
                .thenReturn(new VerificationPair(key, "1234"));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/issueVerificationCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.key").value(key))
                .andExpect(status().isOk());
    }

    @Test
    void issueVerificationCodeWithBadRequest() throws Exception{
        //test with invalid email format
        IssueVerificationCodeRequest request =
                IssueVerificationCodeRequest.builder().email("invalid email").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/issueVerificationCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(status().isBadRequest());
    }


    @Test
    @Transactional
    void changePassword() throws Exception{
        //mock login
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
        mockedStatic.when(AuthUtil::currentUserDetail)
                .thenReturn(new UserDetail(user));
        //test success
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("pa55ward").newPassword("password").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        //clear mock login
        mockedStatic.close();
    }

    @Test
    @Transactional
    void changePasswordWithBadRequest() throws Exception{
        //mock login
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
        mockedStatic.when(AuthUtil::currentUserDetail)
                .thenReturn(new UserDetail(user));
        //test with incorrect old password
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("incorrectolfpassword").newPassword("password").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
        //test with invalid new password
        request = ChangePasswordRequest.builder()
                .oldPassword("pa55ward").newPassword("").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.newPassword").exists())
                .andExpect(status().isBadRequest());
        //clear mock login
        mockedStatic.close();
    }

    @Test
    void forgetPassword() {
    }

    @Test
    @Transactional
    void resetPassword() throws Exception{
        VerifyToken verifyToken=new VerifyToken();
        verifyToken.setUser(user);
        verifyToken.setToken("12345678");
        //valid for 10 min
        verifyToken.setExpireAt(Instant.now().plusSeconds(600));
        verifyTokenRepository.save(verifyToken);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token(verifyToken.getToken()).newPassword("newPassword").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void resetPasswordWithBadRequest() throws Exception{
        //0. validation
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("").newPassword("invalid-password").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.token").exists())
                .andExpect(jsonPath("$.errors.newPassword").exists())
                .andExpect(status().isBadRequest());
        //1. InvalidOperation, with incorrect verifyToken
        VerifyToken verifyToken=new VerifyToken();
        verifyToken.setUser(user);
        verifyToken.setToken("12345678");
        //valid for 10 min
        verifyToken.setExpireAt(Instant.now().plusSeconds(600));
        verifyTokenRepository.save(verifyToken);
        request = ResetPasswordRequest.builder()
                .token("87654321").newPassword("newPassword").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
    }
}