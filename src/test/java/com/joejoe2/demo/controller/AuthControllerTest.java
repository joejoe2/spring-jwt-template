package com.joejoe2.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joejoe2.demo.data.auth.TokenPair;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.auth.request.LoginRequest;
import com.joejoe2.demo.data.auth.request.RefreshRequest;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.model.auth.AccessToken;
import com.joejoe2.demo.model.auth.RefreshToken;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.service.jwt.JwtService;
import com.joejoe2.demo.utils.AuthUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;
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

    ObjectMapper objectMapper=new ObjectMapper();

    @Test
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
    @WithUserDetails(value = "testUser", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void logout() throws Exception{
        //test success
        MockedStatic<AuthUtil> mockedStatic = Mockito.mockStatic(AuthUtil.class);
        mockedStatic.when(AuthUtil::isAuthenticated).thenReturn(true);
        mockedStatic.when(AuthUtil::currentUserDetail)
                .thenReturn(new UserDetail(user.getId().toString(),
                                user.getUserName(),
                                user.isActive(),
                                user.getRole(),
                                "access_token"));
        Mockito.doNothing().when(jwtService).revokeAccessToken("access_token");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout"))
                .andExpect(status().isOk());
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
        mockedStatic.close();
    }

    @Test
    void register() {
    }

    @Test
    void issueVerificationCode() {
    }

    @Test
    @WithUserDetails(value = "testUser", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void changePassword() {
    }

    @Test
    void forgetPassword() {
    }

    @Test
    void resetPassword() {
    }
}