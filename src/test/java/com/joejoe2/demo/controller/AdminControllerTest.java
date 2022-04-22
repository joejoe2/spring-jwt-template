package com.joejoe2.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joejoe2.demo.data.PageList;
import com.joejoe2.demo.data.PageRequest;
import com.joejoe2.demo.data.admin.request.ChangeUserRoleRequest;
import com.joejoe2.demo.data.admin.request.UserIdRequest;
import com.joejoe2.demo.data.user.UserProfile;
import com.joejoe2.demo.exception.InvalidOperation;
import com.joejoe2.demo.exception.UserDoesNotExist;
import com.joejoe2.demo.model.auth.Role;
import com.joejoe2.demo.model.auth.User;
import com.joejoe2.demo.repository.user.UserRepository;
import com.joejoe2.demo.service.user.auth.ActivationService;
import com.joejoe2.demo.service.user.auth.RoleService;
import com.joejoe2.demo.service.user.profile.ProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {
    @MockBean
    RoleService roleService;
    @MockBean
    ActivationService activationService;
    @MockBean
    ProfileService profileService;

    @Autowired
    UserRepository userRepository;

    User user;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void createUser(){
        user=new User();
        user.setUserName("testAdmin");
        user.setRole(Role.ADMIN);
        user.setEmail("testAdmin@email.com");
        user.setPassword("pa55ward");
        userRepository.save(user);
    }

    @AfterEach
    void deleteUser(){
        userRepository.delete(user);
    }

    ObjectMapper objectMapper=new ObjectMapper();

    @Test
    @WithUserDetails(value = "testAdmin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void changeRole() throws Exception{
        //test success
        ChangeUserRoleRequest request=ChangeUserRoleRequest.builder()
                .id(UUID.randomUUID().toString())
                .role(Role.ADMIN.toString())
                .build();
        Mockito.doNothing().when(roleService).changeRoleOf(Mockito.any(), Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        //test 400
        //0. validation
        ChangeUserRoleRequest badRequest=ChangeUserRoleRequest.builder()
                .id("invalid id")
                .role("")
                .build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.errors.id").exists())
                        .andExpect(jsonPath("$.errors.role").exists())
                .andExpect(status().isBadRequest());
        //1. InvalidOperation
        badRequest=ChangeUserRoleRequest.builder()
                .id(UUID.randomUUID().toString())
                .role(Role.ADMIN.toString())
                .build();
        Mockito.doThrow(new InvalidOperation("")).when(roleService).changeRoleOf(Mockito.any(), Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
        //2. UserDoesNotExist
        badRequest=ChangeUserRoleRequest.builder()
                .id(UUID.randomUUID().toString())
                .role(Role.ADMIN.toString())
                .build();
        Mockito.doThrow(new UserDoesNotExist("")).when(roleService).changeRoleOf(Mockito.any(), Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
        //3. role is not exist
        badRequest=ChangeUserRoleRequest.builder()
                .id(UUID.randomUUID().toString())
                .role("not exist role")
                .build();
        Mockito.doNothing().when(roleService).changeRoleOf(Mockito.any(), Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/changeRoleOf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("role is not exist !"));
    }

    @Test
    @WithUserDetails(value = "testAdmin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void activateUser() throws Exception{
        //test success
        UserIdRequest request=UserIdRequest.builder().id(UUID.randomUUID().toString()).build();
        Mockito.doNothing().when(activationService).activateUser(request.getId());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/activateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        //test 400
        //0. validation
        UserIdRequest badRequest=UserIdRequest.builder().id("invalid id").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/activateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.id").exists())
                .andExpect(status().isBadRequest());
        //1. InvalidOperation
        badRequest=UserIdRequest.builder().id(UUID.randomUUID().toString()).build();
        Mockito.doThrow(new InvalidOperation("")).when(activationService).activateUser(badRequest.getId());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/activateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
        //2. UserDoesNotExist
        Mockito.doThrow(new UserDoesNotExist("")).when(activationService).activateUser(badRequest.getId());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/activateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "testAdmin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deactivateUser() throws Exception{
        //test success
        UserIdRequest request=UserIdRequest.builder().id(UUID.randomUUID().toString()).build();
        Mockito.doNothing().when(activationService).deactivateUser(request.getId());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        //test 400
        //0. validation
        UserIdRequest badRequest=UserIdRequest.builder().id("invalid id").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.id").exists())
                .andExpect(status().isBadRequest());
        //1. InvalidOperation
        badRequest=UserIdRequest.builder().id(UUID.randomUUID().toString()).build();
        Mockito.doThrow(new InvalidOperation("")).when(activationService).deactivateUser(badRequest.getId());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
        //2. UserDoesNotExist
        Mockito.doThrow(new UserDoesNotExist("")).when(activationService).deactivateUser(badRequest.getId());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/deactivateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(status().isBadRequest());
    }

    static class CustomResponse{
        public List profiles;

        CustomResponse(List profiles){
            this.profiles=profiles;
        }
    }

    @Test
    @WithUserDetails(value = "testAdmin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getAllUserProfiles() throws Exception{
        List<UserProfile> profiles=new ArrayList<>();
        for (int i=0;i<50;i++){
            User user=new User();
            user.setId(UUID.randomUUID());
            user.setUserName("test"+i);
            user.setEmail("test"+i+"@email.com");
            user.setRole(i%3==0? Role.ADMIN:i%3==1?Role.STAFF:Role.NORMAL);
            user.setActive(i % 2 == 0);
            user.setCreateAt(LocalDateTime.now());
            profiles.add(new UserProfile(user));
        }
        //test success
        Mockito.doReturn(profiles).when(profileService).getAllUserProfiles();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/getUserList")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.profiles").isArray())
                .andExpect(status().isOk()).andReturn();
        assertEquals(objectMapper.writeValueAsString(new CustomResponse(profiles)), result.getResponse().getContentAsString());
        //test success with page request
        PageRequest request=PageRequest.builder().page(2).size(10).build();
        Mockito.doReturn(new PageList<>(10, 2, 5, 10, profiles.subList(20, 30)))
                .when(profileService).getAllUserProfilesWithPage(2, 10);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/getUserList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.profiles").isArray())
                .andExpect(jsonPath("$.totalItems").value(10))
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalPages").value(5))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(status().isOk()).andReturn();
        //test 400
        //0. validation
        PageRequest badRequest=PageRequest.builder().page(-1).size(0).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/getUserList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors.page").exists())
                .andExpect(jsonPath("$.errors.size").exists())
                .andExpect(status().isBadRequest());
    }
}