package com.joejoe2.demo.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joejoe2.demo.TestContext;
import com.joejoe2.demo.config.JwtConfig;
import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.admin.request.ChangeUserRoleRequest;
import com.joejoe2.demo.data.admin.request.UserIdRequest;
import com.joejoe2.demo.data.auth.UserDetail;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.InvalidTokenException;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.service.jwt.JwtService;
import com.joejoe2.demo.service.user.auth.ActivationService;
import com.joejoe2.demo.service.user.auth.RoleService;
import com.joejoe2.demo.service.user.profile.ProfileService;
import com.joejoe2.demo.utils.JwtUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
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
class AdminControllerTest {
  @MockBean RoleService roleService;
  @MockBean ActivationService activationService;
  @MockBean ProfileService profileService;
  @MockBean JwtService jwtService;
  @Autowired JwtConfig jwtConfig;
  @Autowired UserRepository userRepository;

  User admin;
  String adminAccessToken;
  User user;
  String userAccessToken;

  @Autowired MockMvc mockMvc;

  @BeforeEach
  void createUser() throws InvalidTokenException {
    admin = new User();
    admin.setUserName("testAdmin");
    admin.setRole(Role.ADMIN);
    admin.setEmail("testAdmin@email.com");
    admin.setPassword("pa55ward");
    userRepository.save(admin);
    user = new User();
    user.setUserName("testUser");
    user.setRole(Role.NORMAL);
    user.setEmail("testUser@email.com");
    user.setPassword("pa55ward");
    userRepository.save(user);
    userRepository.flush();

    Calendar exp = Calendar.getInstance();
    exp.add(Calendar.SECOND, 900);
    adminAccessToken =
        JwtUtil.generateAccessToken(
            jwtConfig.getPrivateKey(), "jti", jwtConfig.getIssuer(), admin, exp);
    userAccessToken =
        JwtUtil.generateAccessToken(
            jwtConfig.getPrivateKey(), "jti", jwtConfig.getIssuer(), user, exp);
    Mockito.doReturn(false).when(jwtService).isAccessTokenInBlackList(Mockito.any());
    Mockito.doReturn(new UserDetail(admin))
        .when(jwtService)
        .getUserDetailFromAccessToken(adminAccessToken);
    Mockito.doReturn(new UserDetail(user))
        .when(jwtService)
        .getUserDetailFromAccessToken(userAccessToken);
  }

  @AfterEach
  void deleteUser() {
    userRepository.deleteById(admin.getId());
    userRepository.deleteById(user.getId());
  }

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void changeRole() throws Exception {
    // test not authenticated
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
    // test not admin
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                .header(HttpHeaders.AUTHORIZATION, userAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
    // test success
    ChangeUserRoleRequest request =
        ChangeUserRoleRequest.builder()
            .id(UUID.randomUUID().toString())
            .role(Role.ADMIN.toString())
            .build();
    Mockito.doNothing().when(roleService).changeRoleOf(Mockito.any(), Mockito.any());
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void changeRoleWithError() throws Exception {
    // test validation
    ChangeUserRoleRequest request =
        ChangeUserRoleRequest.builder().id("invalid id").role("not exist role").build();
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.errors.id").exists())
        .andExpect(jsonPath("$.errors.role").exists())
        .andExpect(status().isBadRequest());
    // test InvalidOperation
    request =
        ChangeUserRoleRequest.builder()
            .id(UUID.randomUUID().toString())
            .role(Role.ADMIN.toString())
            .build();
    Mockito.doThrow(new InvalidOperation(""))
        .when(roleService)
        .changeRoleOf(Mockito.any(), Mockito.any());
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(status().isForbidden());
    // test UserDoesNotExist
    request =
        ChangeUserRoleRequest.builder()
            .id(UUID.randomUUID().toString())
            .role(Role.ADMIN.toString())
            .build();
    Mockito.doThrow(new UserDoesNotExist(""))
        .when(roleService)
        .changeRoleOf(Mockito.any(), Mockito.any());
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(status().isNotFound());
  }

  @Test
  void activateUser() throws Exception {
    // test not authenticated
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/activateUser")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
    // test not admin
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/activateUser")
                .header(HttpHeaders.AUTHORIZATION, userAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
    // test success
    UserIdRequest request = UserIdRequest.builder().id(UUID.randomUUID().toString()).build();
    Mockito.doNothing().when(activationService).activateUser(request.getId());
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/activateUser")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void activateUserWithError() throws Exception {
    // test validation
    UserIdRequest request = UserIdRequest.builder().id("invalid id").build();
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/activateUser")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.errors.id").exists())
        .andExpect(status().isBadRequest());
    // test InvalidOperation
    request = UserIdRequest.builder().id(UUID.randomUUID().toString()).build();
    Mockito.doThrow(new InvalidOperation("")).when(activationService).activateUser(request.getId());
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/activateUser")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(status().isForbidden());
    // test UserDoesNotExist
    Mockito.doThrow(new UserDoesNotExist("")).when(activationService).activateUser(request.getId());
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/activateUser")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(status().isNotFound());
  }

  @Test
  void deactivateUser() throws Exception {
    // test not authenticated
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
    // test not admin
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                .header(HttpHeaders.AUTHORIZATION, userAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
    // test success
    UserIdRequest request = UserIdRequest.builder().id(UUID.randomUUID().toString()).build();
    Mockito.doNothing().when(activationService).deactivateUser(request.getId());
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void deactivateUserWithError() throws Exception {
    // test validation
    UserIdRequest request = UserIdRequest.builder().id("invalid id").build();
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.errors.id").exists())
        .andExpect(status().isBadRequest());
    // test InvalidOperation
    request = UserIdRequest.builder().id(UUID.randomUUID().toString()).build();
    Mockito.doThrow(new InvalidOperation(""))
        .when(activationService)
        .deactivateUser(request.getId());
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(status().isForbidden());
    // test UserDoesNotExist
    Mockito.doThrow(new UserDoesNotExist(""))
        .when(activationService)
        .deactivateUser(request.getId());
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(status().isNotFound());
  }

  @Test
  void getAllUserProfiles() throws Exception {
    // test not authenticated
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/admin/getUserList")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
    // test not admin
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/admin/getUserList")
                .header(HttpHeaders.AUTHORIZATION, userAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
    // prepare data
    List<UserProfile> profiles = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      User user = new User();
      user.setId(UUID.randomUUID());
      user.setUserName("test" + i);
      user.setEmail("test" + i + "@email.com");
      user.setRole(i % 3 == 0 ? Role.ADMIN : i % 3 == 1 ? Role.STAFF : Role.NORMAL);
      user.setActive(i % 2 == 0);
      user.setCreateAt(Instant.now());
      profiles.add(new UserProfile(user));
    }
    // test success
    Mockito.doReturn(new PageList<>(10, 2, 5, 10, profiles.subList(20, 30)))
        .when(profileService)
        .getAllUserProfilesWithPage(2, 10);
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/admin/getUserList?page=2&size=10")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.profiles").isArray())
        .andExpect(jsonPath("$.totalItems").value(10))
        .andExpect(jsonPath("$.currentPage").value(2))
        .andExpect(jsonPath("$.totalPages").value(5))
        .andExpect(jsonPath("$.pageSize").value(10))
        .andExpect(status().isOk());
  }

  @Test
  void getAllUserProfilesWithError() throws Exception {
    // test validation
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/admin/getUserList?page=-1&size=0")
                .header(HttpHeaders.AUTHORIZATION, adminAccessToken)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.errors.page").exists())
        .andExpect(jsonPath("$.errors.size").exists())
        .andExpect(status().isBadRequest());
  }
}
