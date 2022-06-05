package com.joejoe2.demo.controller;

import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.service.jwt.JwtService;
import com.joejoe2.demo.service.user.profile.ProfileService;
import com.joejoe2.demo.utils.AuthUtil;
import com.joejoe2.demo.utils.JwtUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Calendar;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {
    @MockBean
    ProfileService profileService;
    @Autowired
    UserRepository userRepository;
    @MockBean
    JwtService jwtService;
    @Autowired
    JwtConfig jwtConfig;

    User user;
    String userAccessToken;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void createUser() throws InvalidTokenException {
        user=new User();
        user.setUserName("testUser");
        user.setRole(Role.NORMAL);
        user.setEmail("testUser@email.com");
        user.setPassword("pa55ward");
        userRepository.save(user);
        userRepository.flush();

        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, 900);
        userAccessToken = JwtUtil.generateAccessToken(jwtConfig.getPrivateKey(), "jti", jwtConfig.getIssuer(), user, exp);
        Mockito.doReturn(false).when(jwtService).isAccessTokenInBlackList(Mockito.any());
        Mockito.doReturn(new UserDetail(user)).when(jwtService).getUserDetailFromAccessToken(userAccessToken);
    }

    @AfterEach
    void deleteUser(){
        userRepository.delete(user);
    }

    @Test
    void profile() throws Exception{
        //test not authenticated
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/profile"))
                .andExpect(status().isUnauthorized());
        //test success
        Mockito.when(profileService.getProfile(Mockito.any())).thenReturn(new UserProfile(user));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/profile")
                        .header(HttpHeaders.AUTHORIZATION, userAccessToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.profile.username").value(user.getUserName()))
                .andExpect(jsonPath("$.profile.email").value(user.getEmail()))
                .andExpect(jsonPath("$.profile.role").value(user.getRole().toString()))
                .andExpect(jsonPath("$.profile.isActive").value(user.isActive()))
                .andExpect(jsonPath("$.profile.registeredAt").value(user.getCreateAt().toString()));
    }

    @Test
    void profileWithError() throws Exception{
        //test 500
        Mockito.when(profileService.getProfile(Mockito.any())).thenThrow(new UserDoesNotExist(""));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/profile")
                        .header(HttpHeaders.AUTHORIZATION, userAccessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}