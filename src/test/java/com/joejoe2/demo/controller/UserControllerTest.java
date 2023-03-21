package com.joejoe2.demo.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.joejoe2.demo.TestContext;
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
import com.joejoe2.demo.utils.JwtUtil;
import java.util.Calendar;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(TestContext.class)
class UserControllerTest {
  @MockBean ProfileService profileService;
  @Autowired UserRepository userRepository;
  @MockBean JwtService jwtService;
  @Autowired JwtConfig jwtConfig;

  User user;
  String userAccessToken;

  @Autowired MockMvc mockMvc;

  @BeforeEach
  void createUser() throws InvalidTokenException {
    user = new User();
    user.setUserName("testUser");
    user.setRole(Role.NORMAL);
    user.setEmail("testUser@email.com");
    user.setPassword("pa55ward");
    userRepository.save(user);
    userRepository.flush();

    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.SECOND, 900);
    userAccessToken =
        JwtUtil.generateAccessToken(
            jwtConfig.getPrivateKey(), "jti", jwtConfig.getIssuer(), user, exp);
    Mockito.doReturn(false).when(jwtService).isAccessTokenInBlackList(Mockito.any());
    Mockito.doReturn(new UserDetail(user))
        .when(jwtService)
        .getUserDetailFromAccessToken(userAccessToken);
  }

  @AfterEach
  void deleteUser() {
    userRepository.deleteById(user.getId());
  }

  @Test
  void profile() throws Exception {
    // test not authenticated
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/user/profile"))
        .andExpect(status().isUnauthorized());
    // test success
    Mockito.when(profileService.getProfile(Mockito.any())).thenReturn(new UserProfile(user));
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/user/profile")
                .header(HttpHeaders.AUTHORIZATION, userAccessToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId().toString()))
        .andExpect(jsonPath("$.username").value(user.getUserName()))
        .andExpect(jsonPath("$.email").value(user.getEmail()))
        .andExpect(jsonPath("$.role").value(user.getRole().toString()))
        .andExpect(jsonPath("$.isActive").value(user.isActive()))
        .andExpect(jsonPath("$.registeredAt").value(user.getCreateAt().toString()));
  }

  @Test
  void profileWithError() throws Exception {
    // test 500
    Mockito.when(profileService.getProfile(Mockito.any())).thenThrow(new UserDoesNotExist(""));
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/user/profile")
                .header(HttpHeaders.AUTHORIZATION, userAccessToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }
}
